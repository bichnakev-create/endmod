.text
.global my_openat
.type my_openat, %function
my_openat:
    mov x8, #56
    svc #0
    ret

.global my_read
.type my_read, %function
my_read:
    mov x8, #63
    svc #0
    ret

.global my_close
.type my_close, %function
my_close:
    mov x8, #57
    svc #0
    ret

.global my_mprotect
.type my_mprotect, %function
my_mprotect:
    mov x8, #226
    svc #0
    ret