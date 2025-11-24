package Nebula.Android.Nebula_View.RV_Adapters;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import Nebula.Android.Nebula_Data.Repository.Repo_Calls_History;
import Nebula.Android.Nebula_Model.Entitys.Entity_Call;
import Nebula.Android.Nebula_View.Activities.Activity_02_Feed;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Profile_Image;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Voice_Call;
import Nebula.Android.R;

/// @author Ítalo Oliveira Gomes
public class RV_Feed_03_Calls_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final String TAG = "RV_Feed_03_Calls_Adapter";

    private static final int HEADER_VIEW = 0;
    private static final int ITEM_VIEW = 1;

    public RV_Feed_03_Calls_Adapter(List<Entity_Call> calls) {
        this.calls = calls;
    }

    private final List<Entity_Call> calls;

    private final Set<Integer> selectedPositions = new HashSet<>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? HEADER_VIEW : ITEM_VIEW;
    }

    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        switch (viewType) {
            case HEADER_VIEW:
                view = inflater.inflate(R.layout.rv_05_header_calls_title, parent, false);
                return new HeaderView(view);

            default:
                view = inflater.inflate(R.layout.rv_04_item_call, parent, false);
                return new CallViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {

            case HEADER_VIEW:
                HeaderView header = (HeaderView) holder;
                header.contactHeader.setText(R.string.callsHeaderTitle);
                break;

            case ITEM_VIEW:
            default:
                CallViewHolder callHolder = (CallViewHolder) holder;
                int callPosition = position - 1;

                if (callPosition >= 0 && callPosition < calls.size()) {
                    Entity_Call call = calls.get(callPosition);

                    setupCallData(callHolder, call);

                    callHolder.itemView.setBackgroundResource(
                            selectedPositions.contains(callPosition)
                                    ? R.drawable.bg_selected_chat
                                    : 0
                    );

                    setupCallClickListeners(callHolder, call, callPosition, position);
                }
                break;
        }
    }

    private void setupCallData(CallViewHolder holder, Entity_Call call) {

        holder.callContactName.setText(call.getContactName());

        if (call.getDateTimeCall() != null) {
            holder.callTextDate.setText(dateFormat.format(call.getDateTimeCall()));
        } else {
            holder.callTextDate.setText("--:--");
        }
    }

    public void removeSelected() {
        mainHandler.post(() -> {

            Log.e(TAG, "removeSelected chamado");

            List<Integer> sorted = new ArrayList<>(selectedPositions);
            Collections.sort(sorted, Collections.reverseOrder());

            for (int callPosition : sorted) {

                if (callPosition < 0 || callPosition >= calls.size())
                    continue;

                selectedPositions.clear();

                Entity_Call call = calls.get(callPosition);

                Repo_Calls_History.removeCall(call, callPosition);

                calls.remove(callPosition);
            }
        });
    }


    private void setupCallClickListeners(CallViewHolder callHolder, Entity_Call call,
                                         int callPosition, int adapterPosition) {

        callHolder.itemView.setOnLongClickListener(v -> {
            toggleSelection(callPosition, v);
            return true;
        });

        callHolder.itemView.setOnClickListener(v -> {
            if (!selectedPositions.isEmpty()) {

                toggleSelection(callPosition, v);
            }
        });

        callHolder.callButton.setOnClickListener(v -> {
            Activity activity = (Activity) v.getContext();
            new Controller_Voice_Call(activity).performVoiceCall(activity, call.getContactName(), call.getCallID(), call.getCallID(), call.getContactNumber());
        });
    }

    private void toggleSelection(int position, View itemView) {

        if (selectedPositions.contains(position)) {

            selectedPositions.remove(position);
            itemView.setBackgroundResource(0);

            if (selectedPositions.size() < 1) {
                TypedValue outValue = new TypedValue();
                itemView.getContext().getTheme().resolveAttribute(
                        android.R.attr.selectableItemBackground, outValue, true
                );
                itemView.setForeground(ContextCompat.getDrawable(
                        itemView.getContext(),
                        outValue.resourceId
                ));
            }

        } else {
            selectedPositions.add(position);
            itemView.setBackgroundResource(R.drawable.bg_selected_chat);
            itemView.setForeground(null);
        }

        if (itemView.getContext() instanceof Activity_02_Feed) {
            Activity_02_Feed feed = (Activity_02_Feed) itemView.getContext();

            if (selectedPositions.isEmpty()) feed.hideOptionsBar();
            else feed.showOptionsBarFragment03();

        }
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return calls.size() + 2;
    }


    static class HeaderView extends RecyclerView.ViewHolder {
        TextView contactHeader;

        public HeaderView(@NonNull View itemView) {
            super(itemView);
            contactHeader = itemView.findViewById(R.id.callsTitle);
        }
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        TextView callContactName, callTextDate;
        ImageButton profileImage, callButton;

        public CallViewHolder(@NonNull View itemView) {
            super(itemView);
            callContactName = itemView.findViewById(R.id.call_contact_name);
            callTextDate = itemView.findViewById(R.id.call_text_date);
            profileImage = itemView.findViewById(R.id.call_profile_image);
            callButton = itemView.findViewById(R.id.call);
        }
    }
}