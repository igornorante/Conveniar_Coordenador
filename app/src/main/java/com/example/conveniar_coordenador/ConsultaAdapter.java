package com.example.conveniar_coordenador;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.conveniar_coordenador.model.ConsultaItem;

import java.util.List;
import java.util.Locale;

public class ConsultaAdapter extends BaseAdapter {

    private final Context context;
    private final List<ConsultaItem> itens;

    public ConsultaAdapter(Context context, List<ConsultaItem> itens) {
        this.context = context;
        this.itens = itens;
    }

    @Override
    public int getCount() { return itens.size(); }

    @Override
    public ConsultaItem getItem(int position) { return itens.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public boolean isEnabled(int position) { return false; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_consulta_pedido, parent, false);
        }

        ConsultaItem item = itens.get(position);

        TextView txtNumero = convertView.findViewById(R.id.txt_pedido_numero);
        TextView txtStatus = convertView.findViewById(R.id.txt_pedido_status);
        TextView txtTipo = convertView.findViewById(R.id.txt_pedido_tipo);
        TextView txtProjeto = convertView.findViewById(R.id.txt_pedido_projeto);
        TextView txtPessoa = convertView.findViewById(R.id.txt_pedido_pessoa);
        TextView txtData = convertView.findViewById(R.id.txt_pedido_data);
        TextView txtValor = convertView.findViewById(R.id.txt_pedido_valor);

        // Número do pedido
        txtNumero.setText("Pedido #" + item.getNumero());

        // Status com cor
        txtStatus.setText(item.getStatus());
        aplicarCorStatus(txtStatus, item.getStatus());

        // Tipo (só para pagamento)
        if (item.isPagamento() && item.getTipo() != null && !item.getTipo().isEmpty()) {
            txtTipo.setText(item.getTipo());
            txtTipo.setVisibility(View.VISIBLE);
        } else {
            txtTipo.setVisibility(View.GONE);
        }

        // Projeto
        txtProjeto.setText("Projeto: " + item.getProjeto());

        // Pessoa (fornecedor ou favorecido)
        if (item.getPessoa() != null && !item.getPessoa().isEmpty()) {
            txtPessoa.setText(item.getLabelPessoa() + ": " + item.getPessoa());
            txtPessoa.setVisibility(View.VISIBLE);
        } else {
            txtPessoa.setVisibility(View.GONE);
        }

        // Data formatada
        txtData.setText(item.getData());

        // Valor
        txtValor.setText(String.format(Locale.US, "R$ %.2f", item.getValor()));
        txtValor.setTextColor(item.getValor() < 0 ? Color.parseColor("#D32F2F") : Color.parseColor("#2E7D32"));

        return convertView;
    }

    private void aplicarCorStatus(TextView txtStatus, String status) {
        int corFundo;
        int corTexto = Color.WHITE;

        if (status == null) status = "";

        switch (status.toLowerCase()) {
            case "aprovado":
            case "pago":
            case "encerrado":
                corFundo = Color.parseColor("#2E7D32"); // verde
                break;
            case "pendente":
            case "em processamento":
                corFundo = Color.parseColor("#F57F17"); // amarelo escuro
                break;
            case "registrado":
                corFundo = Color.parseColor("#1565C0"); // azul
                break;
            case "cancelado":
            case "rejeitado":
                corFundo = Color.parseColor("#C62828"); // vermelho
                break;
            default:
                corFundo = Color.parseColor("#78909C"); // cinza
                break;
        }

        GradientDrawable badge = new GradientDrawable();
        badge.setColor(corFundo);
        badge.setCornerRadius(12f);
        txtStatus.setBackground(badge);
        txtStatus.setTextColor(corTexto);
    }
}
