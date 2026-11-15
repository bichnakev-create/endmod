#include <jni.h>
#include <stdint.h>
#include <stddef.h>

extern long my_openat(int dirfd, const char *pathname, int flags, int mode);
extern long my_read(int fd, void *buf, size_t count);
extern long my_close(int fd);

#define AT_FDCWD -100
#define O_RDONLY 0

typedef void* (*t_il2cpp_domain_get)(void);
typedef void* (*t_il2cpp_thread_attach)(void* domain);

typedef void* (*t_CurrenciesHandler_GetInstance)(void);
typedef void (*t_CurrenciesHandler_UpdateCurrency)(void* instance, int64_t amount, int32_t type, uint8_t fromStart, uint8_t playAnim, void* economySource, void* method);
typedef void* (*t_EnergyHandler_GetInstance)(void);
typedef void (*t_EnergyHandler_AddEnergy)(void* instance, int32_t amount, void* method);

static uintptr_t s_il2cpp_base = 0;
static t_il2cpp_domain_get s_il2cpp_domain_get = NULL;
static t_il2cpp_thread_attach s_il2cpp_thread_attach = NULL;
static t_CurrenciesHandler_GetInstance s_CurrGetInstance = NULL;
static t_CurrenciesHandler_UpdateCurrency s_CurrUpdateCurrency = NULL;
static t_EnergyHandler_GetInstance s_EnergyGetInstance = NULL;
static t_EnergyHandler_AddEnergy s_EnergyAddEnergy = NULL;

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
    uintptr_t first_base = 0;
    long n;

    while ((n = my_read(fd, buf, sizeof(buf))) > 0) {
        for (long i = 0; i < n; i++) {
            char c = buf[i];
            if (c == '\n' || line_len >= (int)sizeof(line) - 1) {
                line[line_len] = '\0';
                if (my_strstr(line, line_len, "libil2cpp.so", 12) >= 0) {
                    uintptr_t addr = parse_hex(line);
                    if (first_base == 0) first_base = addr;
                    if (my_strstr(line, line_len, "r-xp", 4) >= 0 || my_strstr(line, line_len, "r--p", 4) >= 0) {
                        if (first_base != 0) {
                            my_close(fd);
                            return first_base;
                        }
                    }
                }
                line_len = 0;
            } else {
                line[line_len++] = c;
            }
        }
    }
    my_close(fd);
    return first_base;
}

static int init_bridge(void) {
    if (s_il2cpp_base && s_CurrGetInstance && s_CurrUpdateCurrency) return 1;

    s_il2cpp_base = find_il2cpp_base();
    if (!s_il2cpp_base) return 0;

    /*
     * RVAs with PT_LOAD 0x4000 offset compensation:
     * il2cpp_domain_get = 0x868494
     * il2cpp_thread_attach = 0x868770
     * CurrenciesHandler.GetInstance = 0x3593D64
     * CurrenciesHandler.UpdateCurrency = 0x35948D4
     * EnergyHandler.GetInstance = 0x3641224
     * EnergyHandler.AddEnergy = 0x3641E40
     */
    s_il2cpp_domain_get = (t_il2cpp_domain_get)(s_il2cpp_base + 0x868494);
    s_il2cpp_thread_attach = (t_il2cpp_thread_attach)(s_il2cpp_base + 0x868770);
    s_CurrGetInstance = (t_CurrenciesHandler_GetInstance)(s_il2cpp_base + 0x3593D64);
    s_CurrUpdateCurrency = (t_CurrenciesHandler_UpdateCurrency)(s_il2cpp_base + 0x35948D4);
    s_EnergyGetInstance = (t_EnergyHandler_GetInstance)(s_il2cpp_base + 0x3641224);
    s_EnergyAddEnergy = (t_EnergyHandler_AddEnergy)(s_il2cpp_base + 0x3641E40);

    return 1;
}

static void ensure_thread_attached(void) {
    if (s_il2cpp_domain_get && s_il2cpp_thread_attach) {
        void* domain = s_il2cpp_domain_get();
        if (domain) {
            s_il2cpp_thread_attach(domain);
        }
    }
}

JNIEXPORT void JNICALL
Java_com_mod_menu_ModMenu_applyCurrencies(JNIEnv* env, jclass clazz, jint coins, jint energy) {
    (void)env;
    (void)clazz;
    if (!init_bridge()) return;
    ensure_thread_attached();

    if (coins != 0 && s_CurrGetInstance && s_CurrUpdateCurrency) {
        void* cInst = s_CurrGetInstance();
        if (cInst) {
            // Coins = 0
            s_CurrUpdateCurrency(cInst, (int64_t)coins, 0, 0, 0, NULL, NULL);
        }
    }

    if (energy != 0 && s_EnergyGetInstance && s_EnergyAddEnergy) {
        void* eInst = s_EnergyGetInstance();
        if (eInst) {
            s_EnergyAddEnergy(eInst, (int32_t)energy, NULL);
        }
    }
}

JNIEXPORT void JNICALL
Java_com_mod_menu_ModMenu_applyGems(JNIEnv* env, jclass clazz, jint gems) {
    (void)env;
    (void)clazz;
    if (!init_bridge()) return;
    ensure_thread_attached();

    if (gems != 0 && s_CurrGetInstance && s_CurrUpdateCurrency) {
        void* cInst = s_CurrGetInstance();
        if (cInst) {
            // Gem = 1
            s_CurrUpdateCurrency(cInst, (int64_t)gems, 1, 0, 0, NULL, NULL);
        }
    }
}
