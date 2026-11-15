import zipfile
import struct
import os
import zlib

INPUT_APK = '/sdcard/Wspxce/ep_v14_v2signed.apk'
OUTPUT_APK = '/sdcard/Wspxce/ep_v14_aligned.apk'
NEW_SO = '/sdcard/Wspxce/libmodmenu_v14.so'
NEW_DEX = '/root/java_build/classes.dex'

OPTIMIZED_BOOT_CONFIG = b"""gfx-disable-mt-rendering=0
gfx-threading-mode=4
gfx-enable-gfx-jobs=1
force-gfx-jobs=1
vulkan-disable-qualcomm-msaa-workaround=1
wait-for-native-debugger=0
hdr-display-enabled=0
gc-max-time-slice=3
androidStartInFullscreen=1
android-preferred-data-location=1
build-guid=b18367d1611248c6bdd1cf0fc3e8dfde
"""

with open(NEW_SO, 'rb') as f:
    so_data = f.read()
with open(NEW_DEX, 'rb') as f:
    dex_data = f.read()

ALIGNMENT = 4096

def build_aligned_apk():
    print(f"Reading from {INPUT_APK}...")
    with zipfile.ZipFile(INPUT_APK, 'r') as zin, open(OUTPUT_APK, 'wb') as fout:
        entries = []
        for item in zin.infolist():
            name = item.filename
            if name.startswith('META-INF/'):
                continue
            
            if name == 'classes12.dex':
                data = dex_data
                compress = False
            elif name == 'lib/arm64-v8a/libmodmenu.so':
                data = so_data
                compress = False
            elif name == 'assets/bin/Data/boot.config':
                data = OPTIMIZED_BOOT_CONFIG
                compress = True
            else:
                data = zin.read(name)
                compress = (item.compress_type != zipfile.ZIP_STORED)
            
            entries.append((name, data, compress))
        
        cd_records = []
        
        for name, data, compress in entries:
            name_bytes = name.encode('utf-8')
            crc = zlib.crc32(data) & 0xffffffff
            uncomp_size = len(data)
            
            if compress:
                cobj = zlib.compressobj(zlib.Z_DEFAULT_COMPRESSION, zlib.DEFLATED, -15)
                comp_data = cobj.compress(data) + cobj.flush()
                comp_size = len(comp_data)
                method = zipfile.ZIP_DEFLATED
            else:
                comp_data = data
                comp_size = uncomp_size
                method = zipfile.ZIP_STORED
            
            extra = b''
            if not compress:
                header_len = 30 + len(name_bytes)
                current_data_offset = fout.tell() + header_len
                rem = current_data_offset % ALIGNMENT
                if rem != 0:
                    pad_len = ALIGNMENT - rem
                    if pad_len < 4:
                        pad_len += ALIGNMENT
                    extra = struct.pack('<HH', 0xd935, pad_len - 4) + b'\x00' * (pad_len - 4)
            
            lh_offset = fout.tell()
            
            lh = struct.pack('<IHHHHHIIIHH',
                0x04034b50,
                20,
                0,
                method,
                0, 0,
                crc,
                comp_size,
                uncomp_size,
                len(name_bytes),
                len(extra)
            )
            fout.write(lh)
            fout.write(name_bytes)
            if extra:
                fout.write(extra)
            
            if not compress:
                actual_offset = fout.tell()
                assert actual_offset % ALIGNMENT == 0, f"Offset {actual_offset} not aligned for {name}"
            
            fout.write(comp_data)
            
            cd_record = struct.pack('<IHHHHHHIIIHHHHHII',
                0x02014b50,
                20,
                20,
                0,
                method,
                0, 0,
                crc,
                comp_size,
                uncomp_size,
                len(name_bytes),
                0,
                0,
                0,
                0,
                0,
                lh_offset
            )
            cd_records.append((cd_record, name_bytes))
        
        cd_start = fout.tell()
        for cd_record, name_bytes in cd_records:
            fout.write(cd_record)
            fout.write(name_bytes)
        
        cd_size = fout.tell() - cd_start
        cd_count = len(cd_records)
        
        eocd = struct.pack('<IHHHHIIH',
            0x06054b50,
            0,
            0,
            cd_count,
            cd_count,
            cd_size,
            cd_start,
            0
        )
        fout.write(eocd)
        
    print(f"Built {OUTPUT_APK} successfully, size: {os.path.getsize(OUTPUT_APK):,} bytes!")

if __name__ == '__main__':
    build_aligned_apk()
