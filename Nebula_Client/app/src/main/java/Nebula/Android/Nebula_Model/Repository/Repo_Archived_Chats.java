package Nebula.Android.Nebula_Model.Repository;

import java.util.ArrayList;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_02_Chat_Session;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_04_Archived_Adapter;

public class Repo_Archived_Chats {


    private static List<Entity_02_Chat_Session> archivedChats = new ArrayList<>();
    private static RV_Feed_04_Archived_Adapter archivedAdapter;

    public static List<Entity_02_Chat_Session> getArchivedChats() {
        return archivedChats;
    }

    public static void setArchivedChats(List<Entity_02_Chat_Session> chatList) {
        archivedChats = chatList;
        if (archivedAdapter != null) {
            archivedAdapter.notifyDataSetChanged();
        }
    }

    public static RV_Feed_04_Archived_Adapter getArchivedAdapter() {
        return archivedAdapter;
    }

    public static void setArchivedAdapter(RV_Feed_04_Archived_Adapter adapter) {
        archivedAdapter = adapter;
    }

    public static void addArchivedChat(Entity_02_Chat_Session chat) {
        if (chat != null) {
            archivedChats.add(chat);
            if (archivedAdapter != null) {
                archivedAdapter.notifyItemInserted(archivedChats.size() - 1);
            }
        }
    }

    // 👇 Sobrecarga para adicionar vários chats de uma vez
    public static void addArchivedChat(List<Entity_02_Chat_Session> chats) {
        if (chats == null || chats.isEmpty()) return;

        int startPos = archivedChats.size(); // posição inicial antes da adição

        for (Entity_02_Chat_Session chat : chats) {
            if (chat != null) {
                archivedChats.add(chat);
            }
        }

        if (archivedAdapter != null) {
            // Atualiza o adapter para todas as novas inserções
            archivedAdapter.notifyItemRangeInserted(startPos, chats.size());
        }
    }


    public static void removeArchivedChat(Entity_02_Chat_Session chat) {
        if (chat != null) {
            int index = archivedChats.indexOf(chat);
            if (index != -1) {
                archivedChats.remove(index);
                if (archivedAdapter != null) {
                    archivedAdapter.notifyItemRemoved(index);
                }
            }
        }
    }

    public static void removeArchivedChatAt(int position) {
        if (position >= 0 && position < archivedChats.size()) {
            archivedChats.remove(position);
            if (archivedAdapter != null) {
                archivedAdapter.notifyItemRemoved(position);
            }
        }
    }

    public static void archiveChat(Entity_02_Chat_Session chat) {
        if (chat != null) {
            // Remove do repositório principal
            Repo_Chat.removeChat(chat);
            // Adiciona ao repositório de arquivados
            addArchivedChat(chat);
        }
    }

    /**
     * Desarquiva um chat (move do Archived_Chat_Repository para Chat_Repository)
     */
    public static void unarchiveChat(Entity_02_Chat_Session chat) {
        if (chat != null) {
            // Remove do repositório de arquivados
            removeArchivedChat(chat);
            // Adiciona de volta ao repositório principal
            Repo_Chat.addChat(chat);
        }
    }

    /**
     * Arquiva um chat por posição
     */
    public static void archiveChatAt(int position) {
        if (position >= 0 && position < Repo_Chat.getChats().size()) {
            Entity_02_Chat_Session chat = Repo_Chat.getChats().get(position);
            archiveChat(chat);
        }
    }

    /**
     * Desarquiva um chat por posição
     */
    public static void unarchiveChatAt(int position) {
        if (position >= 0 && position < archivedChats.size()) {
            Entity_02_Chat_Session chat = archivedChats.get(position);
            unarchiveChat(chat);
        }
    }

    /**
     * Limpa todos os chats arquivados
     */
    public static void clearAllArchivedChats() {
        archivedChats.clear();
        if (archivedAdapter != null) {
            archivedAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Verifica se um chat está arquivado
     */
    public static boolean isChatArchived(Entity_02_Chat_Session chat) {
        return archivedChats.contains(chat);
    }

    /**
     * Retorna o número de chats arquivados
     */
    public static int getArchivedCount() {
        return archivedChats.size();
    }
}