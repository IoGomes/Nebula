package Nebula.Android.Nebula_Data.Repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Nebula.Android.Nebula_Data.LocalDb.DatabaseHelper;
import Nebula.Android.Nebula_Model.Entitys.Entity_Call;
import Nebula.Android.Nebula_Model.Entitys.Entity_Pv_Chat;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_03_Calls_Adapter;

public class Repo_Calls_History {

    private static final String TAG = "Repo_Calls_History";
    private static DatabaseHelper dbHelper;
    private static final String TABLE_NAME = "VideoCallTable";
    private static final String COL_ID = "id";
    private static final String COL_CONTACT_NAME = "nomeDeContato";
    private static final String COL_DATE_TIME = "dateTimeCall";
    private static final String COL_RECEIVED = "received";
    private static List<Entity_Call> calls = new ArrayList<>();

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private static RV_Feed_03_Calls_Adapter feedAdapter;

    public Repo_Calls_History(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public Repo_Calls_History(DatabaseHelper helper) {
        dbHelper = helper;
    }

    /**
     * Inicializa o repositório carregando dados do banco
     */
    public static void initialize(Context context) {
        if (context == null) {
            return;
        }

        dbHelper = new DatabaseHelper(context);
        Repo_Calls_History repo = new Repo_Calls_History(dbHelper);
        repo.loadCallsToRepository();

        Log.i(TAG, "✅ Repo_Calls_History inicializado com " + calls.size() + " chamadas");
    }

    public static List<Entity_Call> getCalls() {
        return calls;
    }

    public static void addCallFromDatabase(Entity_Call call) {
        if (!calls.contains(call)) {
            calls.add(call);
        }
    }

    public static void removeCall(int index) {
        calls.remove(index);
        feedAdapter.notifyItemRemoved(index);
    }

    public static void removeCall(Entity_Call call, int adapterPosition) {
        if (call == null || dbHelper == null) return;

        if (calls.indexOf(call) != -1) {

            calls.remove(call);

            if (feedAdapter != null) {
                feedAdapter.notifyItemRemoved(adapterPosition);

                feedAdapter.notifyItemRangeChanged(adapterPosition,
                        feedAdapter.getItemCount());
            }

            dbHelper.deleteCall(call.getCallID());
        }
    }

    public static void addCall(Entity_Call call) {
        calls.add(call);
        notifyAdapter();
    }

    public static void removeCallAt(int position) {
        if (position >= 0 && position < calls.size()) {
            calls.remove(position);
            notifyAdapter();
        }
    }

    public static void setFeedAdapter(RV_Feed_03_Calls_Adapter adapter) {
        feedAdapter = adapter;
    }

    public static RV_Feed_03_Calls_Adapter getFeedAdapter() {
        return feedAdapter;
    }

    private static void notifyAdapter() {
        if (feedAdapter != null) {
            try {
                feedAdapter.getClass().getMethod("notifyDataSetChanged").invoke(feedAdapter);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao notificar adapter: " + e.getMessage());
            }
        }
    }

    // ==================== MÉTODOS DE BANCO DE DADOS ====================

    /**
     * Insere uma nova chamada no histórico
     */
    public long insertCall(Entity_Call call) {
        SQLiteDatabase db = dbHelper.openDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_CONTACT_NAME, call.getNomeDeContato());
        values.put(COL_DATE_TIME, dateFormat.format(call.getDateTimeCall()));
        values.put(COL_RECEIVED, call.getReiceved() ? 1 : 0);

        long id = db.insert(TABLE_NAME, null, values);
        db.close();

        if (id != -1) {
            addCall(call);
            Log.i(TAG, "📞 Chamada inserida: " + call.getNomeDeContato());
        }

        return id;
    }

    /**
     * Busca todas as chamadas do banco
     */
    public List<Entity_Call> getAllCallsFromDatabase() {
        List<Entity_Call> callsList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_DATE_TIME + " DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Entity_Call call = cursorToCall(cursor);
                if (call != null) {
                    callsList.add(call);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return callsList;
    }

    /**
     * Busca chamadas por nome de contato
     */
    public List<Entity_Call> getCallsByContact(String contactName) {
        List<Entity_Call> callsList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_CONTACT_NAME + " = ? ORDER BY " +
                        COL_DATE_TIME + " DESC",
                new String[]{contactName}
        );

        if (cursor.moveToFirst()) {
            do {
                Entity_Call call = cursorToCall(cursor);
                if (call != null) {
                    callsList.add(call);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return callsList;
    }

    /**
     * Busca apenas chamadas recebidas
     */
    public List<Entity_Call> getReceivedCalls() {
        return getCallsByType(true);
    }

    /**
     * Busca apenas chamadas realizadas
     */
    public List<Entity_Call> getMadeCalls() {
        return getCallsByType(false);
    }

    /**
     * Busca chamadas por tipo (recebidas ou realizadas)
     */
    private List<Entity_Call> getCallsByType(boolean received) {
        List<Entity_Call> callsList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_RECEIVED + " = ? ORDER BY " +
                        COL_DATE_TIME + " DESC",
                new String[]{received ? "1" : "0"}
        );

        if (cursor.moveToFirst()) {
            do {
                Entity_Call call = cursorToCall(cursor);
                if (call != null) {
                    callsList.add(call);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return callsList;
    }

    /**
     * Busca chamadas em um intervalo de datas
     */
    public List<Entity_Call> getCallsBetweenDates(Date startDate, Date endDate) {
        List<Entity_Call> callsList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openDatabase();

        String start = dateFormat.format(startDate);
        String end = dateFormat.format(endDate);

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_DATE_TIME + " BETWEEN ? AND ? ORDER BY " +
                        COL_DATE_TIME + " DESC",
                new String[]{start, end}
        );

        if (cursor.moveToFirst()) {
            do {
                Entity_Call call = cursorToCall(cursor);
                if (call != null) {
                    callsList.add(call);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return callsList;
    }

    /**
     * Deleta uma chamada específica por ID
     */
    public boolean deleteCall(long id) {
        SQLiteDatabase db = dbHelper.openDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, COL_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();

        if (rowsDeleted > 0) {
            loadCallsToRepository();
            Log.i(TAG, "🗑️ Chamada deletada: ID " + id);
        }

        return rowsDeleted > 0;
    }

    /**
     * Deleta todas as chamadas de um contato
     */
    public int deleteCallsByContact(String contactName) {
        SQLiteDatabase db = dbHelper.openDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, COL_CONTACT_NAME + " = ?",
                new String[]{contactName});
        db.close();

        if (rowsDeleted > 0) {
            loadCallsToRepository();
            Log.i(TAG, "🗑️ " + rowsDeleted + " chamadas deletadas de: " + contactName);
        }

        return rowsDeleted;
    }

    /**
     * Deleta todo o histórico de chamadas
     */
    public int deleteAllCalls() {
        SQLiteDatabase db = dbHelper.openDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, null, null);
        db.close();

        if (rowsDeleted > 0) {
            calls.clear();
            notifyAdapter();
            Log.i(TAG, "🗑️ Todo histórico deletado: " + rowsDeleted + " registros");
        }

        return rowsDeleted;
    }

    /**
     * Conta o total de chamadas
     */
    public int getTotalCallsCount() {
        SQLiteDatabase db = dbHelper.openDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /**
     * Conta chamadas de um contato específico
     */
    public int getCallsCountByContact(String contactName) {
        SQLiteDatabase db = dbHelper.openDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COL_CONTACT_NAME + " = ?",
                new String[]{contactName}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /**
     * Carrega todas as chamadas do banco para o repositório em memória
     */
    public void loadCallsToRepository() {
        List<Entity_Call> callsFromDb = getAllCallsFromDatabase();
        calls.clear();

        for (Entity_Call call : callsFromDb) {
            addCallFromDatabase(call);
        }

        Log.i(TAG, "📞 " + calls.size() + " chamadas carregadas no repositório");
    }

    /**
     * Converte um cursor em Entity_05_call
     */
    private Entity_Call cursorToCall(Cursor cursor) {
        try {
            String contactName = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_NAME));
            String dateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_TIME));
            int receivedInt = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RECEIVED));

            Date dateTime = dateFormat.parse(dateTimeStr);
            Boolean received = receivedInt == 1;

            return new Entity_Call(contactName, dateTime, received);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter cursor: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}