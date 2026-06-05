package com.example.conveniar_coordenador;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.conveniar_coordenador.model.PendenciaGrupo;
import com.example.conveniar_coordenador.model.PendenciaStatus;

import java.util.List;

public class PendenciasAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<PendenciaGrupo> grupos;

    public PendenciasAdapter(Context context, List<PendenciaGrupo> grupos) {
        this.context = context;
        this.grupos = grupos;
    }

    @Override
    public int getGroupCount() {
        return grupos.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return grupos.get(groupPosition).getStatusList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return grupos.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return grupos.get(groupPosition).getStatusList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        PendenciaGrupo grupo = (PendenciaGrupo) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group_pendencia, null);
        }

        TextView txtCount = convertView.findViewById(R.id.txt_count_grupo);
        TextView txtTitulo = convertView.findViewById(R.id.txt_titulo_grupo);
        ImageView imgSeta = convertView.findViewById(R.id.img_seta_grupo);

        txtCount.setText(String.valueOf(grupo.getTotal()));
        txtTitulo.setText(grupo.getTitulo());

        if (isExpanded) {
            imgSeta.setImageResource(android.R.drawable.arrow_up_float);
        } else {
            imgSeta.setImageResource(android.R.drawable.arrow_down_float);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        PendenciaStatus status = (PendenciaStatus) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_pendencia, null);
        }

        TextView txtCount = convertView.findViewById(R.id.txt_count_status);
        TextView txtNome = convertView.findViewById(R.id.txt_nome_status);
        LinearLayout layoutBadge = convertView.findViewById(R.id.layout_badge);
        ImageView imgIcon = convertView.findViewById(R.id.img_icon_status);

        txtCount.setText(String.valueOf(status.getQuantidade()));
        txtNome.setText(status.getNome());

        // Cores e Ícones baseados no tipo (Mock conforme imagem)
        switch (status.getTipo()) {
            case "envio":
                layoutBadge.setBackgroundResource(R.drawable.badge_aguardando_envio);
                txtNome.setTextColor(Color.parseColor("#E67E22"));
                imgIcon.setColorFilter(Color.parseColor("#E67E22"));
                break;
            case "aprovacao":
                layoutBadge.setBackgroundResource(R.drawable.badge_aguardando_aprovacao);
                txtNome.setTextColor(Color.parseColor("#3498DB"));
                imgIcon.setColorFilter(Color.parseColor("#3498DB"));
                break;
            case "ajuste":
                layoutBadge.setBackgroundResource(R.drawable.badge_devolvido_ajuste);
                txtNome.setTextColor(Color.parseColor("#E74C3C"));
                imgIcon.setColorFilter(Color.parseColor("#E74C3C"));
                break;
            default:
                layoutBadge.setBackgroundResource(R.drawable.borda_arredondada_status);
                txtNome.setTextColor(Color.BLACK);
                break;
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
