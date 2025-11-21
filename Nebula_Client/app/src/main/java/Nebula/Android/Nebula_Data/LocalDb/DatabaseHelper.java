package Nebula.Android.Nebula_Data.LocalDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Nebula.Android.Nebula_Data.Repository.Repo_Calls_History;
import Nebula.Android.Nebula_Data.Repository.Repo_Contact;
import Nebula.Android.Nebula_Model.Entitys.Entity_Pv_Chat;
import Nebula.Android.Nebula_Data.Repository.Repo_Chat;
import Nebula.Android.Nebula_Model.Entitys.Entity_Call;
import Nebula.Android.Nebula_Model.Entitys.Entity_Contact;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String USER_TABLE = "userTable";
    private static final String VIDEO_CALL_TABLE = "VideoCallTable";
    private static final String VOICE_CALL_TABLE = "voiceCallTable";
    private static final String MESSAGE_CALL_TABLE = "MessageCallTable";


    public enum databaseTablesAndCollumns {
        USER_TABLE,
        VIDEO_CALL_TABLE,
        VOICE_CALL_TABLE,



    }


    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "NebulaLocalDB.db";
    private static final int DB_VERSION = 1;

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    private String getDatabasePath() {
        return context.getDatabasePath(DB_NAME).getPath();
    }



    private void populateSampleData(SQLiteDatabase db) {

        Cursor checkCursor = db.rawQuery("SELECT COUNT(*) FROM ChatSessionTable", null);
        if (checkCursor.moveToFirst()) {
            int existingCount = checkCursor.getInt(0);

            if (existingCount > 0) {
                checkCursor.close();
                return;
            }
        }
        checkCursor.close();

        String[] nomes = {"Lucas Borges", "Ana Castela", "Ana Mariana Braga", "Carlos Eduardo", "João", "Bianca Teles", "Pedro Nunes", "Julia", "Rafael", "Fernanda"};
        String[] mensagens = {
                "Olá! Como você está?",
                "Vamos nos encontrar hoje?",
                "Estou chegando em 10 minutos!",
                "Pode falar, estou livre agora",
                "Até mais! Foi ótimo conversar",
                "Ok, combinado então!",
                "Beleza! Nos falamos depois",
                "Bom dia! Tudo bem com você?",
                "Oi! Tudo certo para amanhã?",
                "Perfeito! Até logo!"
        };

        for (int i = 0; i < 10; i++) {
            ContentValues values = new ContentValues();
            values.put("chatSessionId", "sessao_" + i);
            values.put("chatWith", nomes[i]);
            values.put("hasUnread", (i % 2));
            values.put("isFavorited", (i % 3 == 0) ? 1 : 0);
            values.put("lastMessage", mensagens[i]);

            long result = db.insert("ChatSessionTable", null, values);
        }
    }

    public SQLiteDatabase openDatabase() {
        return this.getWritableDatabase();
    }

    public void loadChatsToRepository() {

        List<Entity_Pv_Chat> chatsFromDb = getAllChatSessions();

        Repo_Chat.getChats().clear();

        for (Entity_Pv_Chat chat : chatsFromDb) {
            Repo_Chat.addChatFromDatabase(chat);
        }
    }

    public void syncChatToRepository(String chatSessionId) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ChatSessionTable WHERE chatSessionId = ?",
                new String[]{chatSessionId});

        if (cursor.moveToFirst()) {
            Entity_Pv_Chat chat = new Entity_Pv_Chat();
            chat.setChatSessionId(cursor.getString(cursor.getColumnIndexOrThrow("chatSessionId")));
            chat.setChatWith(cursor.getString(cursor.getColumnIndexOrThrow("chatWith")));
            chat.setHasUnread(cursor.getInt(cursor.getColumnIndexOrThrow("hasUnread")) == 1);
            chat.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow("isFavorited")) == 1);
            chat.setLastMessage(cursor.getString(cursor.getColumnIndexOrThrow("lastMessage")));

            Repo_Chat.addChat(chat);
            Log.i(TAG, "Chat sincronizado: " + chatSessionId);
        }

        cursor.close();
        db.close();
    }

    public List<Entity_Pv_Chat> getAllChatSessions() {
        List<Entity_Pv_Chat> chats = new ArrayList<>();
        SQLiteDatabase db = openDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ChatSessionTable", null);

        if (cursor.moveToFirst()) {
            do {
                Entity_Pv_Chat chat = new Entity_Pv_Chat();
                chat.setChatSessionId(cursor.getString(cursor.getColumnIndexOrThrow("chatSessionId")));
                chat.setChatWith(cursor.getString(cursor.getColumnIndexOrThrow("chatWith")));
                chat.setHasUnread(cursor.getInt(cursor.getColumnIndexOrThrow("hasUnread")) == 1);
                chat.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow("isFavorited")) == 1);
                chat.setLastMessage(cursor.getString(cursor.getColumnIndexOrThrow("lastMessage")));
                chats.add(chat);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return chats;
    }

    public void insertChat(Entity_Pv_Chat chat) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put("chatSessionId", chat.getChatSessionId());
        values.put("chatWith", chat.getChatWith());
        values.put("hasUnread", chat.hasUnread() ? 1 : 0);
        values.put("isFavorited", chat.isFavorite() ? 1 : 0);
        values.put("lastMessage", chat.getLastMessage());
        db.insert("ChatSessionTable", null, values);
        db.close();
        syncChatToRepository(chat.getChatSessionId());
    }

    public void deleteChat(String chatSessionId) {

        SQLiteDatabase db = openDatabase();
        db.delete("ChatSessionTable", "chatSessionId = ?", new String[]{chatSessionId});
        db.close();

        List<Entity_Pv_Chat> chats = Repo_Chat.getChats();

        for (int i = chats.size() - 2; i >= 0; i--) {
            if (chats.get(i).getChatSessionId().equals(chatSessionId)) {
                Repo_Chat.removeChat(i);
            }
        }
    }

    public void deleteCall(String chatSessionId) {

        Log.e(TAG, "passou pelo DBHelper");

        SQLiteDatabase db = openDatabase();
        db.delete("VideoCallTable", "id = ?", new String[]{chatSessionId});
        db.close();

        List<Entity_Call> calls = Repo_Calls_History.getCalls();

        for (int i = calls.size() - 2; i >= 0; i--) {
            if (calls.get(i).getCallID().equals(chatSessionId)) {
                Repo_Calls_History.removeCall(i);
            }
        }
    }

    public void updateChat(Entity_Pv_Chat chat) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put("chatWith", chat.getChatWith());
        values.put("hasUnread", chat.hasUnread() ? 1 : 0);
        values.put("isFavorited", chat.isFavorite() ? 1 : 0);
        values.put("lastMessage", chat.getLastMessage());
        db.update("ChatSessionTable", values, "chatSessionId = ?", new String[]{chat.getChatSessionId()});
        db.close();

        List<Entity_Pv_Chat> chats = Repo_Chat.getChats();
        for (int i = 0; i < chats.size(); i++) {
            if (chats.get(i).getChatSessionId().equals(chat.getChatSessionId())) {
                chats.set(i, chat);
                if (Repo_Chat.getFeedAdapter() != null) {
                    Repo_Chat.getFeedAdapter().notifyItemChanged(i);
                }
                break;
            }
        }
    }

    public int getUnreadCount() {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM ChatSessionTable WHERE hasUnread = 1", null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();

        Log.i(TAG, "📬 Total de mensagens não lidas: " + count);
        return count;
    }

    public void markChatAsRead(String chatId) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put("hasUnread", 0); // 0 = lido

        db.update(
                "ChatSessionTable",
                values,
                "chatSessionId = ?",
                new String[]{chatId}
        );

        db.close();

        Log.i(TAG, "✅ Chat " + chatId + " marcado como lido");
    }

    public List<Entity_Contact> getAllContacts() {
        List<Entity_Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = openDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ContactTable", null);

        if (cursor.moveToFirst()) {
            do {
                Entity_Contact contact = new Entity_Contact();
                contact.setContactId(cursor.getString(cursor.getColumnIndexOrThrow("contactId")));
                contact.setContactName(cursor.getString(cursor.getColumnIndexOrThrow("contactName")));
                contact.setContactNumber(cursor.getString(cursor.getColumnIndexOrThrow("contactPhone")));
                // Adicione outros campos conforme sua Entity
                contacts.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contacts;
    }

    private void populateContactsData(SQLiteDatabase db) {

        Cursor checkCursor = db.rawQuery("SELECT COUNT(*) FROM ContactTable", null);
        if (checkCursor.moveToFirst()) {
            int existingCount = checkCursor.getInt(0);
            if (existingCount > 0) {
                checkCursor.close();
                return;
            }
        }
        checkCursor.close();

        String[] nomes = {
                "Lucas Silva", "Ana Costa", "Mariana Santos", "Carlos Oliveira",
                "João Pereira", "Bianca Almeida", "Pedro Souza", "Julia Martins",
                "Rafael Lima", "Fernanda Rocha", "Bruno Carvalho", "Camila Ferreira",
                "Diego Ribeiro", "Eduarda Mendes", "Felipe Barbosa", "Gabriela Dias",
                "Henrique Araújo", "Isabela Correia", "José Cardoso", "Larissa Teixeira"
        };

        String[] telefones = {
                "+55 11 98765-4321", "+55 11 97654-3210", "+55 21 96543-2109", "+55 21 95432-1098",
                "+55 11 94321-0987", "+55 31 93210-9876", "+55 31 92109-8765", "+55 41 91098-7654",
                "+55 41 90987-6543", "+55 51 89876-5432", "+55 51 88765-4321", "+55 61 87654-3210",
                "+55 71 86543-2109", "+55 71 85432-1098", "+55 81 84321-0987", "+55 81 83210-9876",
                "+55 85 82109-8765", "+55 85 81098-7654", "+55 91 90987-6543", "+55 91 99876-5432"
        };

        for (int i = 0; i < nomes.length; i++) {
            ContentValues values = new ContentValues();
            values.put("contactId", "contact_" + i);
            values.put("contactName", nomes[i]);
            values.put("contactPhone", telefones[i]);

            long result = db.insert("ContactTable", null, values);
            if (result != -1) {
                Log.i(TAG, "📇 Contato inserido: " + nomes[i]);
            }
        }

        Log.i(TAG, "✅ " + nomes.length + " contatos de exemplo inseridos!");
    }

    public void insertContact(Entity_Contact contact) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put("contactId", contact.getContactId());
        values.put("contactName", contact.getContactName());
        values.put("contactPhone", contact.getContactNumber());

        db.insert("ContactTable", null, values);
        db.close();
        syncContactToRepository(contact.getContactId());
    }

    public void deleteContact(String contactId) {
        SQLiteDatabase db = openDatabase();
        db.delete("ContactTable", "contactId = ?", new String[]{contactId});
        db.close();

        List<Entity_Contact> contacts = Repo_Contact.getContacts();
        for (int i = contacts.size() - 2; i >= 0; i--) {
            if (contacts.get(i).getContactId().equals(contactId)) {
                Repo_Contact.removeContactAt(i);
                break;
            }
        }
    }

    public void updateContact(Entity_Contact contact) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put("contactName", contact.getContactName());
        values.put("contactPhone", contact.getContactNumber());
        // Adicione outros campos conforme sua Entity

        db.update("ContactTable", values, "contactId = ?", new String[]{contact.getContactId()});
        db.close();

        List<Entity_Contact> contacts = Repo_Contact.getContacts();
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getContactId().equals(contact.getContactId())) {
                contacts.set(i, contact);
                if (Repo_Contact.getFeedAdapter() != null) {
                    Repo_Contact.getFeedAdapter().notifyItemChanged(i);
                }
                break;
            }
        }
    }

    /**
     * Busca todas as chamadas do histórico ordenadas por data (mais recente primeiro)
     */
    public List<Entity_Call> getAllCallHistory() {
        List<Entity_Call> calls = new ArrayList<>();
        SQLiteDatabase db = openDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM VideoCallTable ORDER BY dateTimeCall DESC",
                null
        );

        if (cursor.moveToFirst()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            do {
                try {
                    String contactName = cursor.getString(cursor.getColumnIndexOrThrow("nomeDeContato"));
                    String dateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("dateTime"));
                    int isReceived = cursor.getInt(cursor.getColumnIndexOrThrow("isReceived"));

                    Date dateTime = dateFormat.parse(dateTimeStr);

                    Entity_Call call = new Entity_Call(contactName, dateTime, isReceived == 1);
                    calls.add(call);

                } catch (Exception e) {
                    Log.e(TAG, "Erro ao converter chamada: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.i(TAG, "📞 " + calls.size() + " chamadas recuperadas do banco");
        return calls;
    }

    public void syncContactToRepository(String contactId) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ContactTable WHERE contactId = ?",
                new String[]{contactId});

        if (cursor.moveToFirst()) {
            Entity_Contact contact = new Entity_Contact();
            contact.setContactId(cursor.getString(cursor.getColumnIndexOrThrow("contactId")));
            contact.setContactName(cursor.getString(cursor.getColumnIndexOrThrow("contactName")));
            contact.setContactNumber(cursor.getString(cursor.getColumnIndexOrThrow("contactPhone")));

            Repo_Contact.addContactFromDatabase(contact);
            Log.i(TAG, "Contact sincronizado: " + contactId);
        }

        cursor.close();
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    private void populateCallHistoryData(SQLiteDatabase db) {

        Cursor checkCursor = db.rawQuery("SELECT COUNT(*) FROM VideoCallTable", null);
        if (checkCursor.moveToFirst()) {
            int existingCount = checkCursor.getInt(0);
            if (existingCount > 0) {
                checkCursor.close();
                return;
            }
        }
        checkCursor.close();

        String[] contatos = {
                "Lucas Silva", "Ana Costa", "Mariana Santos", "Carlos Oliveira",
                "João Pereira", "Bianca Almeida", "Pedro Souza", "Julia Martins",
                "Rafael Lima", "Fernanda Rocha", "Bruno Carvalho", "Camila Ferreira",
                "Diego Ribeiro", "Eduarda Mendes", "Felipe Barbosa"
        };

        long currentTime = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000L;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (int i = 0; i < 30; i++) {
            ContentValues values = new ContentValues();

            String contato = contatos[i % contatos.length];
            long randomOffset = (long) (Math.random() * 7 * oneDay);
            Date callDate = new Date(currentTime - randomOffset);
            boolean isReceived = (i % 3 != 0);

            values.put("nomeDeContato", contato);
            values.put("dateTimeCall", dateFormat.format(callDate));
            values.put("received", isReceived ? 1 : 0);

            long newId = db.insert("VideoCallTable", null, values); // ID gerado aqui

            if (newId != -1) {
                Log.i(TAG, "📞 Chamada inserida (ID: " + newId + "): " + contato +
                        (isReceived ? " (recebida)" : " (realizada)"));
            }
        }

        Log.i(TAG, "✅ 30 chamadas de exemplo inseridas!");
    }


    public void copyDatabaseIfNeeded() {
        File dbFile = new File(getDatabasePath());

        if (!dbFile.exists()) {
            dbFile.getParentFile().mkdirs();
            try {
                InputStream input = context.getAssets().open(DB_NAME);
                OutputStream output = new FileOutputStream(dbFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }

                output.flush();
                output.close();
                input.close();

                SQLiteDatabase db = SQLiteDatabase.openDatabase(
                        getDatabasePath(),
                        null,
                        SQLiteDatabase.OPEN_READWRITE
                );

                populateSampleData(db);
                populateContactsData(db);
                populateCallHistoryData(db);
                db.close();

                loadChatsToRepository();
                loadCallsToRepository();

            } catch (Exception e) {
                Log.e(TAG, "Erro ao copiar banco: " + e.getMessage());
            }
        } else {
            Log.i(TAG, "Banco já existe em: " + getDatabasePath());

            loadChatsToRepository();
            loadCallsToRepository();
        }
    }

    private void loadCallsToRepository() {
        Repo_Calls_History repo = new Repo_Calls_History(this);
        repo.loadCallsToRepository();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
