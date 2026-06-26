package com.example.conveniar_coordenador;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conveniar_coordenador.model.PendenciaGrupo;
import com.example.conveniar_coordenador.model.PendenciaStatus;

import java.util.List;

public class PendenciasAdapter extends RecyclerView.Adapter<PendenciasAdapter.ViewHolder> {

    private final List<PendenciaGrupo> grupos;

    public PendenciasAdapter(List<PendenciaGrupo> grupos) {
        this.grupos = grupos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pendencia_grupo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendenciaGrupo grupo = grupos.get(position);
        holder.bind(grupo);
    }

    @Override
    public int getItemCount() {
        return grupos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtCount;
        private final TextView txtTitulo;
        private final ImageView imgChevron;
        private final LinearLayout layoutHeader;
        private final LinearLayout containerStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCount = itemView.findViewById(R.id.txt_count_grupo);
            txtTitulo = itemView.findViewById(R.id.txt_titulo_grupo);
            imgChevron = itemView.findViewById(R.id.img_chevron);
            layoutHeader = itemView.findViewById(R.id.layout_grupo_header);
            containerStatus = itemView.findViewById(R.id.layout_status_container);
        }

        public void bind(PendenciaGrupo grupo) {
            txtCount.setText(String.valueOf(grupo.getTotal()));
            txtTitulo.setText(grupo.getTitulo());

            // Reset state
            containerStatus.setVisibility(View.GONE);
            imgChevron.setRotation(270); // Pointing Right

            layoutHeader.setOnClickListener(v -> {
                boolean isExpanded = containerStatus.getVisibility() == View.VISIBLE;
                if (isExpanded) {
                    containerStatus.setVisibility(View.GONE);
                    imgChevron.animate().rotation(270).setDuration(200).start();
                } else {
                    containerStatus.setVisibility(View.VISIBLE);
                    imgChevron.animate().rotation(360).setDuration(200).start();
                }
            });

            containerStatus.removeAllViews();
            for (PendenciaStatus status : grupo.getStatusList()) {
                View statusView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.item_pendencia_status, containerStatus, false);
                
                TextView txtStatusCount = statusView.findViewById(R.id.txt_count_status);
                TextView txtStatusNome = statusView.findViewById(R.id.txt_nome_status);
                LinearLayout layoutBadge = statusView.findViewById(R.id.layout_badge);
                ImageView imgIcon = statusView.findViewById(R.id.img_status_icon);

                txtStatusCount.setText(String.valueOf(status.getQuantidade()));
                txtStatusNome.setText(status.getNome());

                int bgColor, txtColor, iconRes;
                switch (status.getTipo()) {
                    case "envio":
                        bgColor = R.color.pendencia_laranja_bg;
                        txtColor = R.color.pendencia_laranja_txt;
                        iconRes = android.R.drawable.ic_menu_recent_history;
                        break;
                    case "aprovacao":
                        bgColor = R.color.pendencia_azul_bg;
                        txtColor = R.color.pendencia_azul_txt;
                        iconRes = android.R.drawable.ic_dialog_info;
                        break;
                    case "ajuste":
                        bgColor = R.color.pendencia_vermelho_bg;
                        txtColor = R.color.pendencia_vermelho_txt;
                        iconRes = android.R.drawable.ic_delete;
                        break;
                    default:
                        bgColor = R.color.cinza_claro;
                        txtColor = R.color.cinza_texto;
                        iconRes = android.R.drawable.ic_dialog_info;
                }

                layoutBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), bgColor)));
                txtStatusNome.setTextColor(ContextCompat.getColor(itemView.getContext(), txtColor));
                imgIcon.setImageResource(iconRes);
                imgIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), txtColor)));

                containerStatus.addView(statusView);
            }
        }
    }
}
