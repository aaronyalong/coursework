    .comm i, 8, 8
    .globl main
main:
    enter $(8 * 4), $0
     // NOP
    movq $10, %rax
    movq %rax, -8(%rbp)
    movq -8(%rbp), %rax
     // NOP
    movq (%av1), %rax
    movq %rax, -16(%rbp)
    movq -16(%rbp), %rdi
    call printInt
    leave
    ret
