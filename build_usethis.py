import zipfile
import struct
import os
import zlib

INPUT_APK = '/sdcard/Wspxce/usethis.apk'
OUTPUT_APK = '/sdcard/Wspxce/usethis_mod_aligned.apk'
MOD_C10 = '/root/dex_work/classes10_v6.dex'
MOD_C12 = '/root/java_build/classes.dex'
MOD_SO = '/sdcard/Wspxce/libmodmenu_v13.so'

with open(MOD_C10, 'rb') as f:
    c10_data = f.read()
with open(MOD_C12, 'rb') as f:
    c12_data = f.read()
with open(MOD_SO, 'rb') as f:
    so_data = f.read()

ALIGNMENT = 4096

def build_aligned_apk():
    print(f"Reading from {INPUT_APK}...")
    with zipfile.ZipFile(INPUT_APK, 'r') as zin, open(OUTPUT_APK, 'wb') as fout:
        entries = []
        for item in zin.infolist():
            name = item.filename
            if name.startswith('META-INF/'):
                continue
            
            if name == 'classes10.dex':
                data = c10_data
                compress = False
            elif name == 'lib/arm64-v8a/libmodmenu.so':
                data = so_data
                compress = False
            else:
                data = zin.read(name)
                compress = (item.compress_type != zipfile.ZIP_STORED)
            
            entries.append((name, data, compress))
        
        # Inject classes12.dex and libmodmenu.so if not already present
        names_present = {e[0] for e in entries}
        if 'classes12.dex' not in names_present:
            entries.append(('classes12.dex', c12_data, False))
        if 'lib/arm64-v8a/libmodmenu.so' not in names_present:
            entries.append(('lib/arm64-v8a/libmodmenu.so', so_data, False))
        
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