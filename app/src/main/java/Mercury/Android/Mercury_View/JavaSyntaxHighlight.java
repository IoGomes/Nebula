import java.util.HashMap;
import java.util.Map;

public class JavaSyntaxHighlight {

    private static final Map<String, String> COLORS = new HashMap<>();

    static {
        COLORS.put("KEYWORD", "#CC7832");           // Laranja - palavras-chave
        COLORS.put("STRING", "#6A8759");
        COLORS.put("COMMENT", "#808080");
        COLORS.put("NUMBER", "#6897BB");
        COLORS.put("ANNOTATION", "#BBB529");
        COLORS.put("CLASS", "#A9B7C6");
        COLORS.put("METHOD", "#FFC66D");
        COLORS.put("FIELD", "#9876AA");
        COLORS.put("PARAM", "#A9B7C6");
    }
    public static String highlightCode(String code) {
        String result = code;

        // 1. Comentários (deve ser primeiro para não interferir com outros padrões)
        result = highlightComments(result);

        // 2. Strings
        result = highlightStrings(result);

        // 3. Anotações
        result = highlightAnnotations(result);

        // 4. Palavras-chave
        result = highlightKeywords(result);

        // 5. Números
        result = highlightNumbers(result);

        // 6. Classes (tipos com primeira letra maiúscula)
        result = highlightClasses(result);

        // 7. Métodos
        result = highlightMethods(result);

        return result;
    }

    private static String highlightComments(String code) {
        // Comentários de linha
        code = code.replaceAll("(//.*?)(\n|$)",
                "<font color='" + COLORS.get("COMMENT") + "'>$1</font>$2");

        // Comentários de bloco
        code = code.replaceAll("(/\\*.*?\\*/)",
                "<font color='" + COLORS.get("COMMENT") + "'>$1</font>");

        return code;
    }

    private static String highlightStrings(String code) {
        // Strings entre aspas duplas
        code = code.replaceAll("(\"(?:[^\"\\\\]|\\\\.)*\")",
                "<font color='" + COLORS.get("STRING") + "'>$1</font>");

        // Caracteres entre aspas simples
        code = code.replaceAll("('(?:[^'\\\\]|\\\\.)*')",
                "<font color='" + COLORS.get("STRING") + "'>$1</font>");

        return code;
    }

    private static String highlightAnnotations(String code) {
        code = code.replaceAll("(@\\w+)",
                "<font color='" + COLORS.get("ANNOTATION") + "'>$1</font>");

        return code;
    }

    private static String highlightKeywords(String code) {
        String[] keywords = {
                "abstract", "assert", "boolean", "break", "byte", "case", "catch",
                "char", "class", "const", "continue", "default", "do", "double",
                "else", "enum", "extends", "final", "finally", "float", "for",
                "goto", "if", "implements", "import", "instanceof", "int", "interface",
                "long", "native", "new", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super", "switch", "synchronized",
                "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
        };

        for (String keyword : keywords) {
            code = code.replaceAll("\\b(" + keyword + ")\\b",
                    "<font color='" + COLORS.get("KEYWORD") + "'>$1</font>");
        }

        return code;
    }

    private static String highlightNumbers(String code) {
        code = code.replaceAll("\\b(\\d+\\.?\\d*[fFdDlL]?)\\b",
                "<font color='" + COLORS.get("NUMBER") + "'>$1</font>");

        return code;
    }

    private static String highlightClasses(String code) {
        // Classes/Tipos começando com letra maiúscula
        code = code.replaceAll("\\b([A-Z]\\w*)\\b",
                "<font color='" + COLORS.get("CLASS") + "'>$1</font>");

        return code;
    }

    private static String highlightMethods(String code) {
        // Métodos seguidos de parênteses
        code = code.replaceAll("\\b([a-z]\\w*)(?=\\s*\\()",
                "<font color='" + COLORS.get("METHOD") + "'>$1</font>");

        return code;
    }

    /**
     * Gera HTML completo com o código destacado
     */
    public static String generateHTML(String code) {
        String highlightedCode = highlightCode(code);

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body {\n" +
                "            background-color: #2B2B2B;\n" +
                "            font-family: 'JetBrains Mono', 'Consolas', monospace;\n" +
                "            font-size: 14px;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        pre {\n" +
                "            color: #A9B7C6;\n" +
                "            margin: 0;\n" +
                "            white-space: pre-wrap;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <pre>" + highlightedCode + "</pre>\n" +
                "</body>\n" +
                "</html>";
    }

    // Exemplo de uso
    public static void main(String[] args) {
        String sampleCode =
                "package com.example;\n\n" +
                        "import java.util.List;\n\n" +
                        "/**\n" +
                        " * Classe de exemplo\n" +
                        " */\n" +
                        "@Override\n" +
                        "public class Example {\n" +
                        "    private static final int MAX_SIZE = 100;\n" +
                        "    private String name;\n\n" +
                        "    public void performVideoCall(Activity activity) {\n" +
                        "        if (useCaseVideoCall.isEnabled()) {\n" +
                        "            // Inicia a chamada de vídeo\n" +
                        "            Intent intent = new Intent(activity, VideoCallActivity.class);\n" +
                        "            activity.startActivity(intent);\n" +
                        "        } else {\n" +
                        "            String[] permissions = {\"CAMERA\", \"AUDIO\"};\n" +
                        "            requestPermissions(activity, permissions, 100);\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";

        System.out.println(highlightCode(sampleCode));
        // Para gerar HTML completo:
        // System.out.println(generateHTML(sampleCode));
    }
}