package com.example.conveniar_coordenador;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.conveniar_coordenador.model.ExtratoItem;

import java.util.List;

public class ExtratoAdapter extends BaseAdapter {

    private static final int TIPO_SALDO_PROJETO = 0;
    private static final int TIPO_RUBRICA_HEADER = 1;
    private static final int TIPO_LANCAMENTO = 2;
    private static final int TIPO_SALDO_RUBRICA = 3;
    private static final int TIPO_SEM_LANCAMENTO = 4;

    private final Context context;
    private final List<ExtratoItem> itens;

    public ExtratoAdapter(Context context, List<ExtratoItem> itens) {
        this.context = context;
        this.itens = itens;
    }

    @Override
    public int getCount() { return itens.size(); }

    @Override
    public ExtratoItem getItem(int position) { return itens.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public int getViewTypeCount() { return 5; }

    @Override
    public int getItemViewType(int position) {
        switch (itens.get(position).getTipo()) {
            case SALDO_PROJETO:     return TIPO_SALDO_PROJETO;
            case RUBRICA_HEADER:    return TIPO_RUBRICA_HEADER;
            case LANCAMENTO:        return TIPO_LANCAMENTO;
            case SALDO_RUBRICA:     return TIPO_SALDO_RUBRICA;
            case SEM_LANCAMENTO:    return TIPO_SEM_LANCAMENTO;
            default:                return TIPO_LANCAMENTO;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        // Desabilita o clique em todos os itens (apenas exibição)
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExtratoItem item = itens.get(position);
        int viewType = getItemViewType(position);
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case TIPO_SALDO_PROJETO:
                return inflateSaldoProjeto(convertView, parent, inflater, item);
            case TIPO_RUBRICA_HEADER:
                return inflateRubricaHeader(convertView, parent, inflater, item);
            case TIPO_LANCAMENTO:
                return inflateLancamento(convertView, parent, inflater, item);
            case TIPO_SALDO_RUBRICA:
                return inflateSaldoRubrica(convertView, parent, inflater, item);
            case TIPO_SEM_LANCAMENTO:
                return inflateSemLancamento(convertView, parent, inflater, item);
            default:
                return inflateLancamento(convertView, parent, inflater, item);
        }
    }

    private View inflateSaldoProjeto(View convertView, ViewGroup parent, LayoutInflater inflater, ExtratoItem item) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_extrato_saldo, parent, false);
        }
        TextView titulo = convertView.findViewById(R.id.txt_saldo_titulo);
        TextView valor = convertView.findViewById(R.id.txt_saldo_valor);
        titulo.setText(item.getTitulo());
        valor.setText("R$ " + item.getValor());
        return convertView;
    }

    private View inflateRubricaHeader(View convertView, ViewGroup parent, LayoutInflater inflater, ExtratoItem item) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_extrato_rubrica, parent, false);
        }
        TextView nome = convertView.findViewById(R.id.txt_rubrica_nome);
        TextView saldo = convertView.findViewById(R.id.txt_rubrica_saldo);
        nome.setText(item.getTitulo());
        saldo.setText("Saldo Anterior: R$ " + item.getSubtitulo());
        return convertView;
    }

    private View inflateLancamento(View convertView, ViewGroup parent, LayoutInflater inflater, ExtratoItem item) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_extrato_lancamento, parent, false);
        }
        TextView data = convertView.findViewById(R.id.txt_lanc_data);
        TextView tipo = convertView.findViewById(R.id.txt_lanc_tipo);
        TextView doc = convertView.findViewById(R.id.txt_lanc_doc);
        TextView valor = convertView.findViewById(R.id.txt_lanc_valor);

        data.setText(item.getTitulo());
        tipo.setText(item.getSubtitulo());
        doc.setText("Doc: " + item.getValor().split("\\|")[0]);

        // Extrai o valor monetário (após o pipe)
        String[] partes = item.getValor().split("\\|");
        if (partes.length > 1) {
            String valorMon = partes[1];
            valor.setText("R$ " + valorMon);
            valor.setTextColor(item.isDebito() ? Color.parseColor("#D32F2F") : Color.parseColor("#2E7D32"));
        } else {
            valor.setText("");
        }

        return convertView;
    }

    private View inflateSaldoRubrica(View convertView, ViewGroup parent, LayoutInflater inflater, ExtratoItem item) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_extrato_saldo_rubrica, parent, false);
        }
        TextView label = convertView.findViewById(R.id.txt_saldo_rubrica_label);
        TextView valor = convertView.findViewById(R.id.txt_saldo_rubrica_valor);
        label.setText(item.getTitulo());
        valor.setText("R$ " + item.getValor());
        return convertView;
    }

    private View inflateSemLancamento(View convertView, ViewGroup parent, LayoutInflater inflater, ExtratoItem item) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_extrato_saldo_rubrica, parent, false);
        }
        TextView label = convertView.findViewById(R.id.txt_saldo_rubrica_label);
        TextView valor = convertView.findViewById(R.id.txt_saldo_rubrica_valor);
        label.setText(item.getTitulo());
        label.setTextColor(Color.parseColor("#90A4AE"));
        valor.setText("");
        return convertView;
    }
}
