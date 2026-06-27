package com.example.conveniar_coordenador;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conveniar_coordenador.model.Projeto;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProjetosAdapter extends RecyclerView.Adapter<ProjetosAdapter.ViewHolder> {

    private final List<Projeto> projects;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public ProjetosAdapter(List<Projeto> projects) {
        this.projects = projects;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_projeto_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Projeto project = projects.get(position);

        holder.txtCodConvenio.setText(String.valueOf(project.getCodConvenio()));
        holder.txtNomeProjeto.setText(project.getNomeConvenio());
        holder.txtSaldoProjeto.setText(fmt.format(project.getSaldo()));
        holder.txtVigenciaProjeto.setText(project.getDataVigencia());
        holder.txtCoordenadorProjeto.setText("Coordenador: " + project.getCoordenador());
        holder.txtStatusProjeto.setText(project.getNomeStatus());

        // Cor do status
        if ("Ativo".equalsIgnoreCase(project.getNomeStatus())) {
            holder.txtStatusProjeto.setTextColor(Color.parseColor("#2ECC71"));
        } else {
            holder.txtStatusProjeto.setTextColor(Color.parseColor("#E67E22"));
        }
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCodConvenio, txtNomeProjeto, txtSaldoProjeto, txtVigenciaProjeto, txtCoordenadorProjeto, txtStatusProjeto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCodConvenio = itemView.findViewById(R.id.txt_cod_convenio);
            txtNomeProjeto = itemView.findViewById(R.id.txt_nome_projeto);
            txtSaldoProjeto = itemView.findViewById(R.id.txt_saldo_projeto);
            txtVigenciaProjeto = itemView.findViewById(R.id.txt_vigencia_projeto);
            txtCoordenadorProjeto = itemView.findViewById(R.id.txt_coordenador_projeto);
            txtStatusProjeto = itemView.findViewById(R.id.txt_status_projeto);
        }
    }
}
