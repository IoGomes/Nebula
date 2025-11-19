package Nebula.Android.Nebula_Model.Services;
import java.util.Collections;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_Pv_Chat;
import Nebula.Android.Nebula_Data.Repository.Repo_Chat;

///@author Thiago Dantas Carneiro
public class Svc_Search_Query {

    // Remove acentos
    public static String removeAccent(String text) {
        if (text == null) {
            return "";
        }
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalizedText.replaceAll("[^\\p{ASCII}]", "");
    }

    // Limpa caracteres númericos
    private static String clearNumber(String number) {
        if (number == null) {
            return "";
        }
        return number.replaceAll("[^0-9]", "");
    }

    // Verifica se o texto alvo contém o texto pesquisado
    private static boolean containsText(String targetText, String searchText) {
        if (targetText == null || searchText == null) {
            return false;
        }
        String normalizedTarget = removeAccent(targetText.toLowerCase());
        String normalizedSearch = removeAccent(searchText.toLowerCase());
        return normalizedTarget.contains(normalizedSearch);
    }

    // Função principal de pesquisa
    public static List<Entity_Pv_Chat> search(String searchText) {
        List<Entity_Pv_Chat> allChats = Repo_Chat.getChats();
        return search(allChats, searchText);
    }

    // Pesquisa sobrecarregada que aceita lista customizada
    public static List<Entity_Pv_Chat> search(List<Entity_Pv_Chat> chats, String searchText) {
        List<Entity_Pv_Chat> results = new ArrayList<>();
        if (chats == null || searchText == null || searchText.trim().isEmpty()) {
            return new ArrayList<>(chats != null ? chats : new ArrayList<>());
        }
        searchText = searchText.trim();

        for (Entity_Pv_Chat chat : chats) {
            // Null check added
            if (chat == null) continue;

            boolean found = false;

            if (containsText(chat.getChatWith(), searchText)) {
                results.add(chat);
                found = true;
            }

            if (!found && containsText(chat.getLastMessage(), searchText)) {
                results.add(chat);
            }
        }

        sortResults(results);
        return results;
    }

    // Executa pesquisa com integração de filtros por categoria
    public static List<Entity_Pv_Chat> searchWithCategory(String searchText,
                                                          int categoryId,
                                                          int allCategoryId,
                                                          int notReadId,
                                                          int favoriteId) {
        List<Entity_Pv_Chat> allChats = new ArrayList<>(Repo_Chat.getChats());
        List<Entity_Pv_Chat> results = new ArrayList<>();

        if (allChats == null) {
            return results;
        }

        for (Entity_Pv_Chat chat : allChats) {
            // Null check added - skip null entries
            if (chat == null) continue;

            boolean matchesCategory = false;
            boolean matchesSearch;

            // Filtro de categoria
            if (categoryId == allCategoryId) {
                matchesCategory = true;
            } else if (categoryId == notReadId) {
                matchesCategory = chat.hasUnread();
            } else if (categoryId == favoriteId) {
                matchesCategory = chat.isFavorite();
            }

            // Filtro de pesquisa
            if (searchText == null || searchText.trim().isEmpty()) {
                matchesSearch = true;
            } else {
                matchesSearch = containsText(chat.getChatWith(), searchText) ||
                        containsText(chat.getLastMessage(), searchText);
            }

            if (matchesCategory && matchesSearch) {
                results.add(chat);
            }
        }

        // ordena os resultados
        sortResults(results);
        return results;
    }
  
 // Metodo que rdena a lista de chats por prioridade usando Collections.sort (O(n log n)) {Prioridade 1: Mensagens não lidas primeiro ,prioridade 2: Mais recentes primeiro (dentro do mesmo status)}
private static void sortResults(List<Entity_Pv_Chat> chats) {
    chats.removeIf(chat -> chat == null);
    Collections.sort(chats, (c1, c2) -> {
        if (c1.hasUnread() != c2.hasUnread()) {
            return c1.hasUnread() ? -1 : 1;
        }
        return Long.compare(getLastMessageTime(c2), getLastMessageTime(c1));
    });
}

    // Método auxiliar para obter o timestamp da última mensagem
    private static long getLastMessageTime(Entity_Pv_Chat chat) {
        if (chat == null) return 0;

        List<Date> dates = chat.getChatDate();
        if (dates == null || dates.isEmpty()) {
            return 0;
        }
        // Retorna o timestamp da última data (assumindo que a última é a mais recente)
        return dates.get(dates.size() - 1).getTime();
    }

    // Método para pesquisa com filtros adicionais
    public static List<Entity_Pv_Chat> searchWithFilter(String searchText, boolean onlyUnread) {
        return searchWithFilter(Repo_Chat.getChats(), searchText, onlyUnread);
    }

    // Sobrecarga do método de pesquisa com filtros
    public static List<Entity_Pv_Chat> searchWithFilter(List<Entity_Pv_Chat> chats,
                                                        String searchText,
                                                        boolean onlyUnread) {
        // primeiro pesquisa normal
        List<Entity_Pv_Chat> results = search(chats, searchText);

        // se fizer a opção de listar apenas as mensagens não lidas
        if (onlyUnread) {
            List<Entity_Pv_Chat> filtered = new ArrayList<>();
            for (Entity_Pv_Chat chat : results) {
                // Null check added
                if (chat != null && chat.hasUnread()) {
                    filtered.add(chat);
                }
            }
            return filtered;
        }

        return results;
    }

    // Destaca o texto com HTML (para usar com Html.fromHtml)
    public static String highlightTextHtml(String originalText, String searchText, String color) {
        if (originalText == null || searchText == null || searchText.trim().isEmpty()) {
            return originalText;
        }

        String normalizedOriginal = removeAccent(originalText.toLowerCase());
        String normalizedSearch = removeAccent(searchText.toLowerCase());

        int start = normalizedOriginal.indexOf(normalizedSearch);
        if (start == -1) return originalText;

        int end = start + searchText.length();
        if (end > originalText.length()) {
            end = originalText.length();
        }

        return originalText.substring(0, start)
                + "<font color='" + color + "'><b>" + originalText.substring(start, end) + "</b></font>"
                + originalText.substring(end);
    }
}
