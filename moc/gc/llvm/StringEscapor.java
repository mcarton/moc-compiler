package moc.gc.llvm;

/** This class exists only to lighten Machine. */
class StringEscapor {
    /**
     * Escape the string: remove the quotes, apart from '\\' and '\hexa' llvm
     * does not have any escape sequence.
     */
    static String escape(String unescaped) {
        StringBuffer sb = new StringBuffer(unescaped.length());

        boolean backslash = false;
        for (int i = 1; i < unescaped.length()-1; ++i) { // exludes ""
            switch (unescaped.charAt(i)) {
                case '\\':
                    if (backslash) {
                        sb.append("\\\\");
                    }
                    backslash = !backslash;
                    break;
                case 'n':
                    sb.append(backslash ? "\\0A" : "n");
                    backslash = false;
                    break;
                case 't':
                    sb.append(backslash ? "\\09" : "t");
                    backslash = false;
                    break;
                case '"':
                    sb.append(backslash ? "\\22" : "\"");
                    backslash = false;
                    break;
                default:
                    sb.append(unescaped.charAt(i));
                    backslash = false;
            }
        }

        return sb.toString();
    }

    /** Escape the character: llvm does not have characters, the function
     * returns the ascii value.
     */
    static int escapeChar(String unescaped) {
        if (unescaped.charAt(1) == '\\') {
            switch (unescaped.charAt(2)) {
                case '\\': return '\\';
                case 'n' : return '\n';
                case 't' : return '\t';
                case '"' : return '\"';
                default  : return unescaped.charAt(2);
            }
        }
        else {
            return unescaped.charAt(1);
        }
    }
}

