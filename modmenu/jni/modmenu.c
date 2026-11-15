#include <jni.h>
#include <stdint.h>
#include <stddef.h>

extern long my_openat(int dirfd, const char *pathname, int flags, int mode);
extern long my_read(int fd, void *buf, size_t count);
extern long my_close(int fd);
extern long my_mprotect(void *addr, size_t len, int prot);

#define AT_FDCWD -100
#define O_RDONLY 0

#define PROT_READ 1
#define PROT_WRITE 2
#define PROT_EXEC 4

#define PAGE_SIZE 4096
#define PAGE_MASK (~(PAGE_SIZE - 1))

typedef void* (*t_il2cpp_domain_get)(void);
typedef void* (*t_il2cpp_thread_attach)(void* domain);
typedef void* (*t_il2cpp_string_new)(const char* str);
typedef void* (*t_CurrenciesHandler_GetInstance)(void);
typedef void (*t_CurrenciesHandler_UpdateCurrency)(void* instance, int64_t amount, int32_t type, uint8_t fromStart, uint8_t playAnim, void* economySource, void* method);
typedef void* (*t_EnergyHandler_GetInstance)(void);
typedef int32_t (*t_EnergyHandler_get_Energy)(void* instance, void* method);
typedef void (*t_EnergyHandler_ChangeEnergy)(void* instance, int32_t amount, void* economySource, void* method);
typedef void* (*t_SaveManager_get_Instance)(void);
typedef void (*t_SaveManager_ForceSave)(void* instance, void* method);

static uintptr_t s_il2cpp_base = 0;
static t_il2cpp_domain_get s_il2cpp_domain_get = NULL;
static t_il2cpp_thread_attach s_il2cpp_thread_attach = NULL;
static t_il2cpp_string_new s_il2cpp_string_new = NULL;
static t_CurrenciesHandler_GetInstance s_CurrGetInstance = NULL;
static t_CurrenciesHandler_UpdateCurrency s_CurrUpdateCurrency = NULL;
static t_EnergyHandler_GetInstance s_EnergyGetInstance = NULL;
static t_EnergyHandler_get_Energy s_EnergyGetEnergy = NULL;
static t_EnergyHandler_ChangeEnergy s_EnergyChangeEnergy = NULL;
static t_SaveManager_get_Instance s_SaveManagerGetInstance = NULL;
static t_SaveManager_ForceSave s_SaveManagerForceSave = NULL;

typedef int32_t (*t_SaveLoadManager_GetChapter)(void);
typedef void (*t_SaveLoadManager_SetChapter)(int32_t value);
typedef int32_t (*t_SaveLoadManager_GetFakeChapter)(void);
typedef void (*t_SaveLoadManager_SetFakeChapter)(int32_t value);
typedef int32_t (*t_SaveLoadManager_GetWave)(void);
typedef void (*t_SaveLoadManager_SetWave)(int32_t wave);
typedef int32_t (*t_SaveLoadManager_GetLevel)(void);
typedef void (*t_SaveLoadManager_SetLevel)(int32_t level);
typedef void (*t_SaveLoadManager_SyncSkillSlotCountWithChapter)(void);

static t_SaveLoadManager_GetChapter s_SaveLoadManager_GetChapter = NULL;
static t_SaveLoadManager_SetChapter s_SaveLoadManager_SetChapter = NULL;
static t_SaveLoadManager_GetFakeChapter s_SaveLoadManager_GetFakeChapter = NULL;
static t_SaveLoadManager_SetFakeChapter s_SaveLoadManager_SetFakeChapter = NULL;
static t_SaveLoadManager_GetWave s_SaveLoadManager_GetWave = NULL;
static t_SaveLoadManager_SetWave s_SaveLoadManager_SetWave = NULL;
static t_SaveLoadManager_GetLevel s_SaveLoadManager_GetLevel = NULL;
static t_SaveLoadManager_SetLevel s_SaveLoadManager_SetLevel = NULL;
static t_SaveLoadManager_SyncSkillSlotCountWithChapter s_SaveLoadManager_SyncSkillSlotCount = NULL;

// Chest Key Management
typedef int32_t (*t_SaveLoadManager_GetChestValue)(int32_t valueChest, void* method);
typedef void (*t_SaveLoadManager_SetChestValue)(int32_t valueChest, int32_t amount, void* economySource, void* method);
typedef void* (*t_ChestPanelHandler_get_Instance)(void);
typedef void (*t_ChestPanelHandler_RefreshChests)(void* instance, void* method);

// Engine Performance & Optimization
typedef void (*t_Application_set_targetFrameRate)(int32_t fps, void* method);
typedef int32_t (*t_Application_get_targetFrameRate)(void* method);
typedef void (*t_QualitySettings_set_vSyncCount)(int32_t count, void* method);
typedef void (*t_GC_Collect)(void* method);
typedef void (*t_Shader_WarmupAllShaders)(void* method);

static t_SaveLoadManager_GetChestValue s_SaveLoadManager_GetChestValue = NULL;
static t_SaveLoadManager_SetChestValue s_SaveLoadManager_SetChestValue = NULL;
static t_ChestPanelHandler_get_Instance s_ChestPanelHandler_getInstance = NULL;
static t_ChestPanelHandler_RefreshChests s_ChestPanelHandler_RefreshChests = NULL;

static t_Application_set_targetFrameRate s_AppSetTargetFrameRate = NULL;
static t_Application_get_targetFrameRate s_AppGetTargetFrameRate = NULL;
static t_QualitySettings_set_vSyncCount s_QualitySetVSyncCount = NULL;
static t_GC_Collect s_GC_Collect = NULL;
static t_Shader_WarmupAllShaders s_Shader_WarmupAllShaders = NULL;

// -------------------------------------------------------------------------
// No-Ads & 2x Rewarded Hooks State
// -------------------------------------------------------------------------
#define RVA_AD_IS_NO_ADS          0x35CC2AC
#define RVA_SHOP_IS_NO_ADS        0x35CC3AC
#define RVA_SHOW_REWARDED         0x35CE520

static uint32_t orig_Ad_IsNoAds[2] = {0};
static uint32_t orig_Shop_IsNoAds[2] = {0};
static uint32_t orig_ShowRewarded_head[4] = {0};

static int s_no_ads_active = 0;
static int s_double_reward_active = 0;

static uintptr_t parse_hex(const char* s) {
    uintptr_t v = 0;
    while (*s) {
        char c = *s++;
        if (c >= '0' && c <= '9') v = (v << 4) | (c - '0');
        else if (c >= 'a' && c <= 'f') v = (v << 4) | (c - 'a' + 10);
        else if (c >= 'A' && c <= 'F') v = (v << 4) | (c - 'A' + 10);
        else break;
    }
    return v;
}

static int my_strstr(const char* haystack, int len, const char* needle, int needle_len) {
    if (needle_len > len) return -1;
    for (int i = 0; i <= len - needle_len; i++) {
        int match = 1;
        for (int j = 0; j < needle_len; j++) {
            if (haystack[i + j] != needle[j]) {
                match = 0;
                break;
            }
        }
        if (match) return i;
    }
    return -1;
}

static uintptr_t find_il2cpp_base(void) {
    int fd = (int)my_openat(AT_FDCWD, "/proc/self/maps", O_RDONLY, 0);
    if (fd < 0) return 0;
    char buf[4096];
    char line[512];
    int line_len = 0;
    uintptr_t real_base = 0;
    long n;

    while ((n = my_read(fd, buf, sizeof(buf))) > 0) {
        for (long i = 0; i < n; i++) {
            char c = buf[i];
            if (c == '\n' || line_len >= (int)sizeof(line) - 1) {
                line[line_len] = '\0';
                if (my_strstr(line, line_len, "libil2cpp.so", 12) >= 0) {
                    if (my_strstr(line, line_len, "r-xp", 4) >= 0) {
                        uintptr_t rx_addr = parse_hex(line);
                        real_base = rx_addr - 0x02da1000;
                        my_close(fd);
                        return real_base;
                    }
                }
                line_len = 0;
            } else {
                line[line_len++] = c;
            }
        }
    }
    my_close(fd);
    return real_base;
}

static int make_page_writable(void* addr, size_t len) {
    uintptr_t start = (uintptr_t)addr & PAGE_MASK;
    uintptr_t end = ((uintptr_t)addr + len + PAGE_SIZE - 1) & PAGE_MASK;
    return (my_mprotect((void*)start, end - start, PROT_READ | PROT_WRITE | PROT_EXEC) == 0);
}

static void clear_arm64_cache(void* addr, size_t len) {
    uintptr_t start = (uintptr_t)addr;
    uintptr_t end = start + len;
    for (uintptr_t p = start & ~63; p < end; p += 64) {
        __asm__ volatile("dc cvau, %0" : : "r"(p) : "memory");
    }
    __asm__ volatile("dsb ish" : : : "memory");
    for (uintptr_t p = start & ~63; p < end; p += 64) {
        __asm__ volatile("ic ivau, %0" : : "r"(p) : "memory");
    }
    __asm__ volatile("dsb ish" : : : "memory");
    __asm__ volatile("isb" : : : "memory");
}

static int init_bridge(void) {
    if (s_il2cpp_base && s_CurrGetInstance && s_CurrUpdateCurrency) return 1;

    s_il2cpp_base = find_il2cpp_base();
    if (!s_il2cpp_base) return 0;

    s_il2cpp_domain_get = (t_il2cpp_domain_get)(s_il2cpp_base + 0x3253ba8);
    s_il2cpp_thread_attach = (t_il2cpp_thread_attach)(s_il2cpp_base + 0x3254004);
    s_il2cpp_string_new = (t_il2cpp_string_new)(s_il2cpp_base + 0x3253fe8);

    s_CurrGetInstance = (t_CurrenciesHandler_GetInstance)(s_il2cpp_base + 0x3597d64);
    s_CurrUpdateCurrency = (t_CurrenciesHandler_UpdateCurrency)(s_il2cpp_base + 0x35988d4);

    s_EnergyGetInstance = (t_EnergyHandler_GetInstance)(s_il2cpp_base + 0x3645224);
    s_EnergyGetEnergy = (t_EnergyHandler_get_Energy)(s_il2cpp_base + 0x3645204);
    s_EnergyChangeEnergy = (t_EnergyHandler_ChangeEnergy)(s_il2cpp_base + 0x3645c68);

    s_SaveManagerGetInstance = (t_SaveManager_get_Instance)(s_il2cpp_base + 0x35915a4);
    s_SaveManagerForceSave = (t_SaveManager_ForceSave)(s_il2cpp_base + 0x3592120);

    s_SaveLoadManager_GetChapter = (t_SaveLoadManager_GetChapter)(s_il2cpp_base + 0x35ACE64);
    s_SaveLoadManager_SetChapter = (t_SaveLoadManager_SetChapter)(s_il2cpp_base + 0x35ACDD0);
    s_SaveLoadManager_GetFakeChapter = (t_SaveLoadManager_GetFakeChapter)(s_il2cpp_base + 0x35ACF70);
    s_SaveLoadManager_SetFakeChapter = (t_SaveLoadManager_SetFakeChapter)(s_il2cpp_base + 0x35ACF00);
    s_SaveLoadManager_GetWave = (t_SaveLoadManager_GetWave)(s_il2cpp_base + 0x35AC588);
    s_SaveLoadManager_SetWave = (t_SaveLoadManager_SetWave)(s_il2cpp_base + 0x35AC518);
    s_SaveLoadManager_GetLevel = (t_SaveLoadManager_GetLevel)(s_il2cpp_base + 0x35AC8D8);
    s_SaveLoadManager_SetLevel = (t_SaveLoadManager_SetLevel)(s_il2cpp_base + 0x35AC868);
    s_SaveLoadManager_SyncSkillSlotCount = (t_SaveLoadManager_SyncSkillSlotCountWithChapter)(s_il2cpp_base + 0x35B0920);

    // Chest Keys RVAs
    s_SaveLoadManager_GetChestValue = (t_SaveLoadManager_GetChestValue)(s_il2cpp_base + 0x35B0FDC);
    s_SaveLoadManager_SetChestValue = (t_SaveLoadManager_SetChestValue)(s_il2cpp_base + 0x35B10D0);
    s_ChestPanelHandler_getInstance = (t_ChestPanelHandler_get_Instance)(s_il2cpp_base + 0x33DC988);
    s_ChestPanelHandler_RefreshChests = (t_ChestPanelHandler_RefreshChests)(s_il2cpp_base + 0x33DD484);

    // Engine Performance RVAs
    s_AppSetTargetFrameRate = (t_Application_set_targetFrameRate)(s_il2cpp_base + 0x6AA4A28);
    s_AppGetTargetFrameRate = (t_Application_get_targetFrameRate)(s_il2cpp_base + 0x6AA4A00);
    s_QualitySetVSyncCount = (t_QualitySettings_set_vSyncCount)(s_il2cpp_base + 0x6ABC900);
    s_GC_Collect = (t_GC_Collect)(s_il2cpp_base + 0x5EDF6BC);
    s_Shader_WarmupAllShaders = (t_Shader_WarmupAllShaders)(s_il2cpp_base + 0x6AC34EC);

    // Save original instructions for Ad patching
    orig_Ad_IsNoAds[0] = ((uint32_t*)(s_il2cpp_base + RVA_AD_IS_NO_ADS))[0];
    orig_Ad_IsNoAds[1] = ((uint32_t*)(s_il2cpp_base + RVA_AD_IS_NO_ADS))[1];

    orig_Shop_IsNoAds[0] = ((uint32_t*)(s_il2cpp_base + RVA_SHOP_IS_NO_ADS))[0];
    orig_Shop_IsNoAds[1] = ((uint32_t*)(s_il2cpp_base + RVA_SHOP_IS_NO_ADS))[1];

    uint32_t* pRew = (uint32_t*)(s_il2cpp_base + RVA_SHOW_REWARDED);
    orig_ShowRewarded_head[0] = pRew[0];
    orig_ShowRewarded_head[1] = pRew[1];
    orig_ShowRewarded_head[2] = pRew[2];
    orig_ShowRewarded_head[3] = pRew[3];

    return 1;
}

static void ensure_thread_attached(void) {
    if (!init_bridge()) return;
    if (s_il2cpp_domain_get && s_il2cpp_thread_attach) {
        void* domain = s_il2cpp_domain_get();
        if (domain) {
            s_il2cpp_thread_attach(domain);
        }
    }
}

static void* get_empty_string(void) {
    if (s_il2cpp_string_new) {
        return s_il2cpp_string_new("");
    }
    return NULL;
}

static void trigger_save(void) {
    if (s_SaveManagerGetInstance && s_SaveManagerForceSave) {
        void* sInst = s_SaveManagerGetInstance();
        if (sInst) {
            s_SaveManagerForceSave(sInst, NULL);
        }
    }
}

static float read_currency_array(void* cInst, int32_t targetType) {
    uintptr_t arr_ptr = *(uintptr_t*)((uintptr_t)cInst + 0x28);
    if (!arr_ptr) return -1.0f;

    int32_t len = *(int32_t*)(arr_ptr + 0x18);
    if (len <= 0 || len > 100) return -1.0f;

    uintptr_t items = arr_ptr + 0x20;
    for (int32_t i = 0; i < len; i++) {
        uintptr_t item = *(uintptr_t*)(items + i * sizeof(uintptr_t));
        if (item) {
            int32_t t = *(int32_t*)(item + 0x28);
            if (t == targetType) {
                return *(float*)(item + 0x18);
            }
        }
    }
    return -1.0f;
}

// -------------------------------------------------------------------------
// Instant 2x Double Reward Trampoline Hook
// -------------------------------------------------------------------------
static void hooked_ShowRewardedVideo(void* thisPtr, int32_t category, int32_t source, void* itemStr, int32_t rewardAmount, void* onComplete, void* onRewardConfirmed) {
    (void)thisPtr; (void)category; (void)source; (void)itemStr; (void)rewardAmount;
    ensure_thread_attached();

    // 1. Invoke onRewardConfirmed Action()
    if (onRewardConfirmed) {
        uintptr_t invoke_fn = *(uintptr_t*)((uintptr_t)onRewardConfirmed + 0x18);
        void* target = *(void**)((uintptr_t)onRewardConfirmed + 0x40);
        void* method_ptr = *(void**)((uintptr_t)onRewardConfirmed + 0x28);
        if (invoke_fn) {
            typedef void (*t_action_invoke)(void* target, void* method);
            ((t_action_invoke)invoke_fn)(target, method_ptr);
        }
    }

    // 2. Invoke onComplete Action<bool>(true)
    if (onComplete) {
        uintptr_t invoke_fn2 = *(uintptr_t*)((uintptr_t)onComplete + 0x18);
        void* target2 = *(void**)((uintptr_t)onComplete + 0x40);
        void* method_ptr2 = *(void**)((uintptr_t)onComplete + 0x28);
        if (invoke_fn2) {
            typedef void (*t_action_bool_invoke)(void* target, uint8_t val, void* method);
            ((t_action_bool_invoke)invoke_fn2)(target2, 1, method_ptr2);
        }
    }
}

// -------------------------------------------------------------------------
// JNI Export Implementations
// -------------------------------------------------------------------------

JNIEXPORT jboolean JNICALL
Java_com_mod_menu_ModMenu_isNoAdsActive(JNIEnv* env, jclass clazz) {
    (void)env; (void)clazz;
    return (jboolean)s_no_ads_active;
}

JNIEXPORT jboolean JNICALL
Java_com_mod_menu_ModMenu_setNoAdsActive(JNIEnv* env, jclass clazz, jboolean enable) {
    (void)env; (void)clazz;
    if (!init_bridge()) return 0;

    void* addr_ad_noads = (void*)(s_il2cpp_base + RVA_AD_IS_NO_ADS);
    void* addr_shop_noads = (void*)(s_il2cpp_base + RVA_SHOP_IS_NO_ADS);

    if (!make_page_writable(addr_ad_noads, 8) || !make_page_writable(addr_shop_noads, 8)) {
        return 0;
    }

    if (enable) {
        // MOV W0, #1 (0x52800020); RET (0xD65F03C0)
        ((uint32_t*)addr_ad_noads)[0] = 0x52800020;
        ((uint32_t*)addr_ad_noads)[1] = 0xD65F03C0;

        ((uint32_t*)addr_shop_noads)[0] = 0x52800020;
        ((uint32_t*)addr_shop_noads)[1] = 0xD65F03C0;
        s_no_ads_active = 1;
    } else {
        ((uint32_t*)addr_ad_noads)[0] = orig_Ad_IsNoAds[0];
        ((uint32_t*)addr_ad_noads)[1] = orig_Ad_IsNoAds[1];

        ((uint32_t*)addr_shop_noads)[0] = orig_Shop_IsNoAds[0];
        ((uint32_t*)addr_shop_noads)[1] = orig_Shop_IsNoAds[1];
        s_no_ads_active = 0;
    }

    clear_arm64_cache(addr_ad_noads, 8);
    clear_arm64_cache(addr_shop_noads, 8);
    return 1;
}

JNIEXPORT jboolean JNICALL
Java_com_mod_menu_ModMenu_isDoubleRewardActive(JNIEnv* env, jclass clazz) {
    (void)env; (void)clazz;
    return (jboolean)s_double_reward_active;
}

JNIEXPORT jboolean JNICALL
Java_com_mod_menu_ModMenu_setDoubleRewardActive(JNIEnv* env, jclass clazz, jboolean enable) {
    (void)env; (void)clazz;
    if (!init_bridge()) return 0;

    void* addr_rewarded = (void*)(s_il2cpp_base + RVA_SHOW_REWARDED);
    if (!make_page_writable(addr_rewarded, 16)) return 0;

    if (enable) {
        uint32_t hook_code[4];
        hook_code[0] = 0x58000050; // LDR X16, #8
        hook_code[1] = 0xD61F0200; // BR X16
        *(uint64_t*)&hook_code[2] = (uint64_t)&hooked_ShowRewardedVideo;

        for (int i = 0; i < 4; i++) {
            ((uint32_t*)addr_rewarded)[i] = hook_code[i];
        }
        s_double_reward_active = 1;
    } else {
        for (int i = 0; i < 4; i++) {
            ((uint32_t*)addr_rewarded)[i] = orig_ShowRewarded_head[i];
        }
        s_double_reward_active = 0;
    }

    clear_arm64_cache(addr_rewarded, 16);
    return 1;
}

JNIEXPORT jdouble JNICALL
Java_com_mod_menu_ModMenu_getAmount(JNIEnv* env, jclass clazz, jint type) {
    (void)env; (void)clazz;
    if (!init_bridge()) return -1.0;
    ensure_thread_attached();

    if (type == -1) {
        if (!s_EnergyGetInstance || !s_EnergyGetEnergy) return -1.0;
        void* eInst = s_EnergyGetInstance();
        if (!eInst) return -1.0;
        return (jdouble)s_EnergyGetEnergy(eInst, NULL);
    }

    if (!s_CurrGetInstance) return -1.0;
    void* cInst = s_CurrGetInstance();
    if (!cInst) return -1.0;

    float val = read_currency_array(cInst, (int32_t)type);
    return (jdouble)val;
}

JNIEXPORT jboolean JNICALL
Java_com_mod_menu_ModMenu_setAmount(JNIEnv* env, jclass clazz, jint type, jlong targetAmount) {
    (void)env; (void)clazz;
    if (!init_bridge()) return 0;
    ensure_thread_attached();

    void* emptyStr = get_empty_string();

    if (type == -1) {
        if (!s_EnergyGetInstance || !s_EnergyGetEnergy || !s_EnergyChangeEnergy) return 0;
        void* eInst = s_EnergyGetInstance();
        if (!eInst) return 0;
        int32_t currentEnergy = s_EnergyGetEnergy(eInst, NULL);
        int32_t diff = (int32_t)targetAmount - currentEnergy;
        s_EnergyChangeEnergy(eInst, diff, emptyStr, NULL);
        trigger_save();
        return 1;
    }

    if (!s_CurrGetInstance || !s_CurrUpdateCurrency) return 0;
    void* cInst = s_CurrGetInstance();
    if (!cInst) return 0;

    float currentVal = read_currency_array(cInst, (int32_t)type);
    if (currentVal < 0.0f) currentVal = 0.0f;

    int64_t diff = (int64_t)targetAmount - (int64_t)currentVal;
    s_CurrUpdateCurrency(cInst, diff, (int32_t)type, 0, 0, emptyStr, NULL);
    trigger_save();
    return 1;
}

JNIEXPORT jfloat JNICALL
Java_com_mod_menu_ModMenu_getCurrencyAmount(JNIEnv* env, jclass clazz, jint type) {
    return (jfloat)Java_com_mod_menu_ModMenu_getAmount(env, clazz, type);
}

JNIEXPORT jboolean JNICALL
Java_com_mod_menu_ModMenu_applyCurrencies(JNIEnv* env, jclass clazz, jint coins, jint energy) {
    (void)env; (void)clazz;
    if (!init_bridge()) return 0;
    ensure_thread_attached();
    void* emptyStr = get_empty_string();

    if (coins != 0 && s_CurrGetInstance && s_CurrUpdateCurrency) {
        void* cInst = s_CurrGetInstance();
        if (cInst) {
            s_CurrUpdateCurrency(cInst, (int64_t)coins, 0, 0, 0, emptyStr, NULL);
        }
    }
    if (energy != 0 && s_EnergyGetInstance && s_EnergyChangeEnergy) {
        void* eInst = s_EnergyGetInstance();
        if (eInst) {
            s_EnergyChangeEnergy(eInst, energy, emptyStr, NULL);
        }
    }
    trigger_save();
    return 1;
}

JNIEXPORT jboolean JNICALL
Java_com_mod_menu_ModMenu_applyGems(JNIEnv* env, jclass clazz, jint gems) {
    (void)env; (void)clazz;
    if (!init_bridge()) return 0;
    ensure_thread_attached();
    if (gems != 0 && s_CurrGetInstance && s_CurrUpdateCurrency) {
        void* cInst = s_CurrGetInstance();
        if (cInst) {
            void* emptyStr = get_empty_string();
            s_CurrUpdateCurrency(cInst, (int64_t)gems, 1, 0, 0, emptyStr, NULL);
            trigger_save();
        }
    }
    return 1;
}

// -------------------------------------------------------------------------
// Chest Key JNI Bridge
// -------------------------------------------------------------------------
JNIEXPORT jint JNICALL
Java_com_mod_menu_ModMenu_getChestKey(JNIEnv* env, jclass clazz, jint chestType) {
    (void)env; (void)clazz;
    if (!init_bridge()) return -1;
    ensure_thread_attached();
    if (s_SaveLoadManager_GetChestValue) {
        return (jint)s_SaveLoadManager_GetChestValue((int32_t)chestType, NULL);
    }
    return -1;
}

JNIEXPORT jboolean JNICALL
Java_com_mod_menu_ModMenu_setChestKey(JNIEnv* env, jclass clazz, jint chestType, jint amount) {
    (void)env; (void)clazz;
    if (!init_bridge()) return 0;
    ensure_thread_attached();

    if (s_SaveLoadManager_SetChestValue) {
        void* emptyStr = get_empty_string();
        s_SaveLoadManager_SetChestValue((int32_t)chestType, (int32_t)amount, emptyStr, NULL);
    }

    if (s_ChestPanelHandler_getInstance && s_ChestPanelHandler_RefreshChests) {
        void* cpInst = s_ChestPanelHandler_getInstance();
        if (cpInst) {
            s_ChestPanelHandler_RefreshChests(cpInst, NULL);
        }
    }

    trigger_save();
    return 1;
}

// -------------------------------------------------------------------------
// Stage / Progression JNI Bridge
// -------------------------------------------------------------------------
JNIEXPORT jint JNICALL
Java_com_mod_menu_ModMenu_getChapter(JNIEnv* env, jclass clazz) {
    (void)env; (void)clazz;
    ensure_thread_attached();
    if (s_SaveLoadManager_GetChapter) {
        return (jint)s_SaveLoadManager_GetChapter();
    }
    return -1;
}

JNIEXPORT jint JNICALL
Java_com_mod_menu_ModMenu_getWave(JNIEnv* env, jclass clazz) {
    (void)env; (void)clazz;
    ensure_thread_attached();
    if (s_SaveLoadManager_GetWave) {
        return (jint)s_SaveLoadManager_GetWave();
    }
    return -1;
}

JNIEXPORT jboolean JNICALL
Java_com_mod_menu_ModMenu_syncStage(JNIEnv* env, jclass clazz, jint targetChapter, jint targetWave) {
    (void)env; (void)clazz;
    ensure_thread_attached();

    if (s_SaveLoadManager_SetChapter) {
        s_SaveLoadManager_SetChapter((int32_t)targetChapter);
    }
    if (s_SaveLoadManager_SetFakeChapter) {
        s_SaveLoadManager_SetFakeChapter((int32_t)targetChapter);
    }
    if (s_SaveLoadManager_SetWave) {
        s_SaveLoadManager_SetWave((int32_t)targetWave);
    }
    if (s_SaveLoadManager_SetLevel) {
        s_SaveLoadManager_SetLevel((int32_t)targetWave);
    }
    if (s_SaveLoadManager_SyncSkillSlotCount) {
        s_SaveLoadManager_SyncSkillSlotCount();
    }
    trigger_save();
    return 1;
}

// -------------------------------------------------------------------------
// Engine Performance & FPS JNI Bridge
// -------------------------------------------------------------------------
JNIEXPORT jboolean JNICALL
Java_com_mod_menu_ModMenu_optimizePerformance(JNIEnv* env, jclass clazz, jint targetFps) {
    (void)env; (void)clazz;
    if (!init_bridge()) return 0;
    ensure_thread_attached();

    // 1. Disable VSync (0) to unlock native high refresh rates (60/90/120Hz)
    if (s_QualitySetVSyncCount) {
        s_QualitySetVSyncCount(0, NULL);
    }

    // 2. Set Target Frame Rate (e.g. 60 or 120 FPS)
    if (s_AppSetTargetFrameRate) {
        s_AppSetTargetFrameRate((int32_t)targetFps, NULL);
    }

    // 3. Precompile & warmup all GPU shader variants to eliminate first-hit shader compile stutters
    if (s_Shader_WarmupAllShaders) {
        s_Shader_WarmupAllShaders(NULL);
    }

    // 4. Trigger immediate GC cleanup to free fragmented allocations
    if (s_GC_Collect) {
        s_GC_Collect(NULL);
    }

    return 1;
}

JNIEXPORT jint JNICALL
Java_com_mod_menu_ModMenu_getTargetFrameRate(JNIEnv* env, jclass clazz) {
    (void)env; (void)clazz;
    if (!init_bridge()) return -1;
    ensure_thread_attached();
    if (s_AppGetTargetFrameRate) {
        return (jint)s_AppGetTargetFrameRate(NULL);
    }
    return -1;
}
