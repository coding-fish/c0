package miniplc0java.util;

import miniplc0java.tokenizer.TokenType;

public enum Ty {//ty -> IDENT
    // 下面这三种也是IDENT而不是关键字，对变量类型，函数返回类型做了约束
    UINT,
    DOUBLE,
    VOID,// 变量不能是void类型的！
    BOOL,// 比如赋值表达式的右侧 是 赋值表达式，应该报错
    FUNC,// 用在全局符号表，函数的返回类型在Function类中单独存放
    STRING,// 字符串字面量，和全局变量放在一起
//    CHAR,
//    FUNC,// 方便符号表里放函数名，但不和全局变量放在一起，因此o0里的偏移需要重新计算
}
