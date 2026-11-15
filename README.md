# Endless Puncher Mod Project - Agent Worklog & Technical Guide

## Project Overview
- **Target Application**: Endless Puncher v1.7.03 (Version Code 67) & v1.7.04 (Version Code 68) (`com.fubugames.endlesspuncher`)
- **Engine**: Unity 2021+ IL2CPP (64-bit ARMv8-a)
- **Target OS**: Android 12+ (tested on HWMAO-Q / Huawei Nova 9 SE, Snapdragon 680 'bengal')
- **Mod Menu**: Cyber Mods in-game dialog activated via 3-finger touch gesture.
- **Repository Location**: Dedicated repository isolated at `/sdcard/Wspxce/endless Mod Files`.

---

## Technical Architecture & Guidelines

### 1. IL2CPP Base Address Resolution
- Under Android 12+, `/proc/self/maps` contains multiple mappings for APK assets (`r--p`) that match the name `libil2cpp.so`.
- **Rule**: Never match only by filename. Filter specifically for the executable segment `r-xp`.
- **Formula**:
  $$\text{Real Base} = \text{rx\_segment\_start} - \text{0x02da1000}$$
- Segment 1 of `libil2cpp.so` has virtual address `0x02da1000` (file offset `0x02d9d000`).

### 2. Thread Safety & Unity Main Thread Dispatch
- All IL2CPP method calls must attach to the runtime via `il2cpp_thread_attach(il2cpp_domain_get())`.
- Direct UI calls from Android threads crash IL2CPP. Dispatch calls through `mUnityPlayer.invokeOnMainThread(Runnable)` via reflection.

### 3. Currency & Energy Handlers
- **CurrencyTypes Enum**:
  - `0`: Coins
  - `1`: Gems
  - `2`: Diamonds
  - `3`: Cubes
  - `4`: Red Gems
  - `5`: Pet Gems
  - `6`: Shop Gems
  - `7`: Rings
  - `8`: Collectible Tokens
  - `9`: Event Pass Tokens
  - `10`: Hero EXP Tokens
  - `-1`: Energy
- **Direct Target Value Setting**:
  - Compute $\Delta = \text{target} - \text{current}$.
  - Apply $\Delta$ using `CurrenciesHandler.UpdateCurrency` or `EnergyHandler.ChangeEnergy`.
  - Pass an empty IL2CPP string (`il2cpp_string_new("")`) for `economySource` to avoid null pointer exceptions.
  - Call `SaveManager.ForceSave()` immediately after applying changes to ensure disk persistence.
- **Direct Memory Array Reading**:
  - To prevent uncaught `Il2CppExceptionWrapper` crashes caused by `CurrenciesHandler.GetAmount` creating heap predicate allocations, read balances directly through `CurrenciesHandler.currencies` (offset `0x28`).
  - Each `Currency` object holds `amount` (float at offset `0x18`) and `type` (int32 at offset `0x28`).

### 4. UI Architecture & High-Performance Tabs
- Mod Menu features snappy category tabs:
  - `ALL`: Complete roster view.
  - `MAIN`: Energy, Coins, Rings.
  - `GEMS`: Gems, Diamonds, Red Gems, Pet Gems, Shop Gems.
  - `SPECIAL`: Cubes, Collectible Tokens, Event Pass Tokens, Hero EXP Tokens.
  - `KEYS`: Level / Stage & Chest manipulation.
- Instant tab filtering dynamically toggles view visibility without rebuilding the view tree, ensuring maximum rendering speed and zero main-thread lag.

### 5. Packaging & Signing Rules
- Store `classes*.dex` and `.so` uncompressed (`ZIP_STORED`).
- Align uncompressed files to 4096-byte boundaries (`zipalign 4096`).
- Sign with APK Signature Scheme v2/v3 using debug keystore.

---

## Mod Update & Version Migration Guide (e.g. v1.7.03 -> v1.7.04)

### Why Addresses Change
When game developers update the game, recompiling C++ in `libil2cpp.so` causes Relative Virtual Addresses (RVAs) of functions to shift slightly. Class memory layouts (struct field offsets) typically remain stable across minor versions, but method entry pointers move.

### Two-Path Upgrade Strategy
1. **Dynamic Resolution Path (Zero Hardcoding)**:
   - Use IL2CPP export APIs (`il2cpp_domain_get`, `il2cpp_domain_assembly_open`, `il2cpp_assembly_get_image`, `il2cpp_class_from_name`, `il2cpp_class_get_method_from_name`).
   - Query function pointers dynamically at runtime from `Assembly-CSharp.dll` without recompiling RVAs.
2. **Surgical RVA Sync Path (Quick Patch)**:
   - Extract `libil2cpp.so` and `global-metadata.dat` from new target APK.
   - Run Il2CppDumper / REA toolchain to generate fresh `dump.cs`.
   - Update the 7 core RVAs (`CurrenciesHandler`, `EnergyHandler`, `SaveManager`, `SaveLoadManager`, `ChestPanelHandler`, `Shader.WarmupAllShaders`, `Application.set_targetFrameRate`) in `mod_native.c`.
   - Recompile `libmodmenu.so` and re-inject `classes12.dex` via `build_v14.py`.

### Repository Structure (`/sdcard/Wspxce/endless Mod Files`)
- `Dump0/`: Complete IL2CPP dump (`dump.cs`, header files, symbol maps, scripts).
- `modmenu/`: Complete Java source tree for Cyber Mod Menu (`ModMenu.java`, layouts, tabs).
- `libil2cpp_game.so` & `global-metadata.dat`: Target game binaries for RVA extraction.
- `mod_native_v14.c` & `libmodmenu_v14.so`: Production native hooking library and source.
- `syscalls.s`: Direct Bionic ARM64 system call stubs.
- `build_v14.py`: Automated APK injection, 4KB page alignment, and packaging pipeline.
- `compile_dex.sh`: Javac + D8 DEX build script.
- `ModBridge.java`, `patch_elephant*.py`, `update_modmenu.py`, `fix_stage_task.py`: Injection and utility scripts.
