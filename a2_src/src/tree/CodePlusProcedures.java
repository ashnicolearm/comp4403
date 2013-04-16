package tree;

public class CodePlusProcedures {
    /** Instruction sequence for program */
    private Code instructionList;
    /** Start addresses of procedures */
    private Procedures procStarts;
    
    public CodePlusProcedures( Code instructionList, Procedures procStarts ) {
        this.instructionList = instructionList;
        this.procStarts = procStarts;
    }
    
    public Code getInstructionList() {
        return instructionList;
    }
    public Procedures getProcStarts() {
        return procStarts;
    }
}
