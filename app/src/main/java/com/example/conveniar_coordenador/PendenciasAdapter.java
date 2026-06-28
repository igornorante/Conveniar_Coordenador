package com.example.conveniar_coordenador;

import android.content.Intent;
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
    private final String token;

    public PendenciasAdapter(List<PendenciaGrupo> grupos, String token) {
        this.grupos = grupos;
        this.token = token;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pendencia_grupo, parent, false);
        return new ViewHolder(view, token);
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
        private final ImageView imgIcone;
        private final LinearLayout layoutHeader;
        private final LinearLayout containerStatus;
        private final String token;

        public ViewHolder(@NonNull View itemView, String token) {
            super(itemView);
            this.token = token;
            txtCount = itemView.findViewById(R.id.txt_count_grupo);
            txtTitulo = itemView.findViewById(R.id.txt_titulo_grupo);
            imgChevron = itemView.findViewById(R.id.img_chevron);
            imgIcone = itemView.findViewById(R.id.img_categoria_icone);
            layoutHeader = itemView.findViewById(R.id.layout_grupo_header);
            containerStatus = itemView.findViewById(R.id.layout_status_container);
        }

        public void bind(PendenciaGrupo grupo) {
            txtCount.setText(String.valueOf(grupo.getTotal()));
            txtTitulo.setText(grupo.getTitulo());

            // Define o ícone com base no título do grupo
            int iconeRes = R.drawable.icone_pedido;
            String t = grupo.getTitulo().toLowerCase();
            if (t.contains("compra")) iconeRes = R.drawable.icone_compra;
            else if (t.contains("bolsa")) iconeRes = R.drawable.icone_pag_bolsa;
            else if (t.contains("diária")) iconeRes = R.drawable.icone_diaria;
            else if (t.contains("adiantamento")) iconeRes = R.drawable.icone_adiantamento;
            else if (t.contains("reembolso")) iconeRes = R.drawable.icone_reembolso;
            else if (t.contains("física")) iconeRes = R.drawable.icone_pag_fisica;
            else if (t.contains("jurídica")) iconeRes = R.drawable.icone_pag_juridica;
            
            imgIcone.setImageResource(iconeRes);

            // Reset state
            containerStatus.setVisibility(View.GONE);
            imgChevron.setRotation(0); 

            layoutHeader.setOnClickListener(v -> {
                boolean isExpanded = containerStatus.getVisibility() == View.VISIBLE;
                if (isExpanded) {
                    containerStatus.setVisibility(View.GONE);
                    imgChevron.animate().rotation(0).setDuration(200).start();
                } else {
                    containerStatus.setVisibility(View.VISIBLE);
                    imgChevron.animate().rotation(180).setDuration(200).start();
                }
            });

            containerStatus.removeAllViews();
            for (PendenciaStatus status : grupo.getStatusList()) {
                View statusView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.item_pendencia_status, containerStatus, false);
                
                TextView txtStatusCount = statusView.findViewById(R.id.txt_count_status);
                TextView txtStatusNome = statusView.findViewById(R.id.txt_nome_status);
                LinearLayout layoutBadge = statusView.findViewById(R.id.layout_badge);
                ImageView imgStatusIcon = statusView.findViewById(R.id.img_status_icon);

                txtStatusCount.setText(String.valueOf(status.getQuantidade()));
                txtStatusNome.setText(status.getNome());

                statusView.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), PendenciasDetalheActivity.class);
                    intent.putExtra("TIPO", grupo.getTitulo());
                    intent.putExtra("STATUS", status.getNome());
                    intent.putExtra("TOKEN", token);
                    v.getContext().startActivity(intent);
                });

                int bgColor, txtColor, iconRes;
                switch (status.getTipo()) {
                    case "ajuste":
                        bgColor = R.color.pendencia_vermelho_bg;
                        txtColor = R.color.pendencia_vermelho_txt;
                        iconRes = android.R.drawable.ic_dialog_alert;
                        break;
                    case "envio":
                        bgColor = R.color.pendencia_laranja_bg;
                        txtColor = R.color.pendencia_laranja_txt;
                        iconRes = android.R.drawable.ic_menu_recent_history;
                        break;
                    default:
                        bgColor = R.color.pendencia_azul_bg;
                        txtColor = R.color.pendencia_azul_txt;
                        iconRes = android.R.drawable.ic_dialog_info;
                }

                layoutBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), bgColor)));
                txtStatusNome.setTextColor(ContextCompat.getColor(itemView.getContext(), txtColor));
                imgStatusIcon.setImageResource(iconRes);
                imgStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), txtColor)));

                containerStatus.addView(statusView);
            }
        }
    }
}
