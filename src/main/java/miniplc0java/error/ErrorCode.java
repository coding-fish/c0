package miniplc0java.error;

import net.sourceforge.argparse4j.internal.UnrecognizedArgumentException;

public enum ErrorCode {
    NoError, // Should be only used internally.
    StreamError, EOF, InvalidInput, InvalidIdentifier, IntegerOverflow, // int32_t overflow.
    NoBegin, NoEnd, NeedIdentifier, ConstantNeedValue, NoSemicolon, InvalidVariableDeclaration, IncompleteExpression,
    NotDeclared, AssignToConstant, DuplicateDeclaration, NotInitialized, InvalidAssignment, InvalidPrint, ExpectedToken,
    UnrecognizedType, TypeUnmatch, VarTypeVoid, NoReturnValue, BreakDenied, ContinueDenied, ElseHaveNoIf
}
