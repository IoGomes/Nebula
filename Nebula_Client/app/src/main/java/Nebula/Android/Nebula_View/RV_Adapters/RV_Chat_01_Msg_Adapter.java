package Nebula.Android.Nebula_View.RV_Adapters;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import Nebula.Android.Nebula_Model.Entitys.Entity_Message;
import Nebula.Android.R;

public class RV_Chat_01_Msg_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<Entity_Message> messages;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public RV_Chat_01_Msg_Adapter(List<Entity_Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Entity_Message message = messages.get(position);
        return (message.isSentByMe() != null && message.isSentByMe())
                ? VIEW_TYPE_SENT
                : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SENT) {

            View view = inflater.inflate(R.layout.rv_02_msg_send, parent, false);
            return new SentMessageViewHolder(view);
        } else {

            View view = inflater.inflate(R.layout.rv_03_msg_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Entity_Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message, dateFormat);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message, dateFormat);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder para mensagens ENVIADAS
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textDate, textVisualized;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textDate = itemView.findViewById(R.id.textDate);
        }

        public void bind(Entity_Message message, SimpleDateFormat dateFormat) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                textMessage.setText(
                        Html.fromHtml(message.getMessage(), Html.FROM_HTML_MODE_LEGACY)
                );
            } else {
                textMessage.setText(
                        Html.fromHtml(message.getMessage())
                );
            }

            // Define horário
            if (message.getDateTimeMessage() != null) {
                textDate.setText(dateFormat.format(message.getDateTimeMessage()));
            } else {
                textDate.setText("--:--");
            }

            if (textVisualized != null) {
                if (message.getWasVisualized() != null && message.getWasVisualized()) {
                    textVisualized.setVisibility(View.VISIBLE);
                } else {
                    textVisualized.setVisibility(View.GONE);
                }
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textDate, textSenderName;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textDate = itemView.findViewById(R.id.textDate);
        }

        public void bind(Entity_Message message, SimpleDateFormat dateFormat) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                textMessage.setText(
                        Html.fromHtml(message.getMessage(), Html.FROM_HTML_MODE_LEGACY)
                );
            } else {
                textMessage.setText(
                        Html.fromHtml(message.getMessage())
                );
            }

            // Define horário
            if (message.getDateTimeMessage() != null) {
                textDate.setText(dateFormat.format(message.getDateTimeMessage()));
            } else {
                textDate.setText("--:--");
            }

            if (textSenderName != null) {
                if (message.getSenderName() != null && !message.getSenderName().isEmpty()) {
                    textSenderName.setText(message.getSenderName());
                    textSenderName.setVisibility(View.VISIBLE);
                } else {
                    textSenderName.setVisibility(View.GONE);
                }
            }
        }
    }
}