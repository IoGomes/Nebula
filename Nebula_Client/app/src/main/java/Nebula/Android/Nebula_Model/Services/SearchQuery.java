import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Scanner;

public class SearchQuery {

    public static class Chat {
        private String id;
        private String name;
        private String number;
        private String lastMessageText;
        private long lastMessageTime;
        private boolean read;

        public Chat(String id, String name, String number, String lastMessageText, long lastMessageTime, boolean read) {
            this.id = id;
            this.name = name;
            this.number = number;
            this.lastMessageText = lastMessageText;
            this.lastMessageTime = lastMessageTime;
            this.read = read;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getNumber() { return number; }
        public String getLastMessageText() { return lastMessageText; }
        public long getLastMessageTime() { return lastMessageTime; }
        public boolean isRead() { return read; }
    }

    public static class Search {
        // Método de remover acentos
        private static String removeAccent(String text) {
            if (text == null) {
                return "";
            }
            String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
            return normalizedText.replaceAll("[^\\p{ASCII}]", "");
        }

        // Método para limpar número(sem caracteres)
        private static String clearNumber(String number) {
            if (number == null) {
                return "";
            }
            return number.replaceAll("[^0-9]", "");
        }

        // Método que verifica se o texto alvo contém o texto pesquisado
        private static boolean containsText(String targetText, String searchText) {
            if (targetText == null || searchText == null) {
                return false;
            }
            String normalizedTarget = removeAccent(targetText.toLowerCase());
            String normalizedSearch = removeAccent(searchText.toLowerCase());
            return normalizedTarget.contains(normalizedSearch);
        }

        // Método principal de pesquisa
        public static ArrayList<Chat> search(ArrayList<Chat> chats, String searchText) {
            ArrayList<Chat> results = new ArrayList<>();
            if (chats == null || searchText == null || searchText.trim().isEmpty()) {
                return results;
            }
            searchText = searchText.trim();

            // pesquisa por nome
            for (Chat chat : chats) {
                if (containsText(chat.getName(), searchText)) {
                    results.add(chat);
                    continue;
                }
                // pesquisa por número
                String clearedNumber = clearNumber(chat.getNumber());
                String clearedSearch = clearNumber(searchText);
                if (clearedNumber.contains(clearedSearch) && !clearedSearch.isEmpty()) {
                    results.add(chat);
                    continue;
                }
                // pesquisa pela ultima mensagem
                if (containsText(chat.getLastMessageText(), searchText)) {
                    results.add(chat);
                }
            }
            // ordena os resultados
            sortResults(results);
            return results;
        }

        // Método para troca de posições dos resultados
        private static void swap(ArrayList<Chat> list, int pos1, int pos2) {
            Chat temp = list.get(pos1);
            list.set(pos1, list.get(pos2));
            list.set(pos2, temp);
        }

        // Método de ordenação Bubble Sort (por prioridade {mensagem não lida --> mensagem lida recente --> mensagem lida antiga})
        private static void sortResults(ArrayList<Chat> chats) {
            for (int i = 0; i < chats.size() - 1; i++) {
                for (int j = 0; j < chats.size() - i - 1; j++) {
                    Chat current = chats.get(j);
                    Chat next = chats.get(j + 1);

                    // Prioridade 1: Mensagens não lidas
                    if (current.isRead() && !next.isRead()) {
                        swap(chats, j, j + 1);
                    }
                    // Prioridade 2: Se ambos tiverem o mesmo status, o mais recente vai primeiro
                    else if (current.isRead() == next.isRead() &&
                            current.getLastMessageTime() < next.getLastMessageTime()) {
                        swap(chats, j, j + 1);
                    }
                }
            }
        }

        // Método para pesquisa com filtros adicionais
        public static ArrayList<Chat> searchWithFilter(ArrayList<Chat> chats,
                                                       String searchText,
                                                       boolean onlyUnread) {
            // primeiro pesquisa normal
            ArrayList<Chat> results = search(chats, searchText);

            // se fizer a opção de listar apenas as mensagens não lidas
            if (onlyUnread) {
                ArrayList<Chat> filtered = new ArrayList<>();
                for (int i = 0; i < results.size(); i++) {
                    if (!results.get(i).isRead()) {
                        filtered.add(results.get(i));
                    }
                }
                return filtered;
            }

            return results;
        }

        // Destaca o texto encontrado pela pesquisa
        public static String highlightText(String originalText, String searchText) {
            if (originalText == null || searchText == null || searchText.trim().isEmpty()) {
                return originalText;
            }

            // Normaliza os dois textos
            String normalizedOriginal = removeAccent(originalText.toLowerCase());
            String normalizedSearch = removeAccent(searchText.toLowerCase());

            // Procura a posição da palavra na mensagem
            int start = normalizedOriginal.indexOf(normalizedSearch);
            if (start == -1) return originalText; // não achou, retorna igual

            int end = start + searchText.length();

            // Agora usamos os mesmos índices no texto original para destacar
            if (end > originalText.length()) {
                end = originalText.length();
            }

            return originalText.substring(0, start)
                    + "[" + originalText.substring(start, end) + "]"
                    + originalText.substring(end);
        }
    }


        //
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        ArrayList<Chat> chats = new ArrayList<>();
        long now = System.currentTimeMillis();

        chats.add(new Chat("1", "João Silva", "+55 (11) 98765-4321", "Olá, tudo bem?", now - 5000, true));
        chats.add(new Chat("2", "José Santos", "+55 21 91234-5678", "Vamos marcar o almoço hoje?", now - 3000, false));
        chats.add(new Chat("3", "Maria José", "11987654321", "Recebi o documento, obrigada!", now - 1000, false));
        chats.add(new Chat("4", "Pedro Costa", "+55 85 99999-8888", "Obrigado pela almoço!", now - 7000, true));
        chats.add(new Chat("5", "Ana Paula", "+55 11 94444-3333", "Quando você pode me ligar?", now - 2000, false));
        chats.add(new Chat("6", "Carlos Eduardo", "21987651234", "Até amanhã!", now - 10000, true));



        while (true) {
            System.out.print("🔍 Buscar (ou 'sair' para encerrar): ");
            String searchText = scanner.nextLine();

            if (searchText.trim().equalsIgnoreCase("sair") ||
                    searchText.trim().equalsIgnoreCase("exit")) {

                break;
            }

            if (searchText.trim().isEmpty()) {
                System.out.println(" Digite algo para buscar!\n");
                continue;
            }

            ArrayList<Chat> results = Search.search(chats, searchText);


            if (results.isEmpty()) {
                System.out.println("Nenhum resultado encontrado para: \"" + searchText + "\"");
            } else {
                System.out.println("Encontrado " + results.size() + " resultado(s) para: \"" + searchText + "\"\n");

                for (int i = 0; i < results.size(); i++) {
                    Chat chat = results.get(i);
                    String status = chat.isRead() ? "Lida" : "Não lida";


                    String highlightedMessage = Search.highlightText(chat.getLastMessageText(), searchText);

                    System.out.println((i + 1) + ". " + status + " | " + chat.getName());
                    System.out.println("    " + chat.getNumber());
                    System.out.println("   " + highlightedMessage);

                    if (i < results.size() - 1) {
                        System.out.println();
                    }
                }
            }

        }

        scanner.close();
    }
}