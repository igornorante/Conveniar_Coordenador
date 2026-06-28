package com.example.conveniar_coordenador;

import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.conveniar_coordenador.database.PedidoEntity;
import com.example.conveniar_coordenador.database.PedidoPagamentoEntity;
import java.util.List;

public class PendenciaDetalheAdapter extends RecyclerView.Adapter<PendenciaDetalheAdapter.ViewHolder> {

    private List<Object> itens;
    private String token;

    public PendenciaDetalheAdapter(List<Object> itens, String token) {
        this.itens = itens;
        this.token = token;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pendencia_detalhe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = itens.get(position);
        String idPedido = "";
        String baseUrl = "";

        if (item instanceof PedidoEntity) {
            PedidoEntity p = (PedidoEntity) item;
            idPedido = p.numPedido;
            baseUrl = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoCompra.aspx";

            holder.txtIdPedido.setText("Pedido #" + p.numPedido);
            holder.txtInfoPrincipal.setText("Projeto: " + p.codProjeto);
            holder.txtInfoSecundaria.setText("Situação: " + p.situacao);
        } else if (item instanceof PedidoPagamentoEntity) {
            PedidoPagamentoEntity p = (PedidoPagamentoEntity) item;
            idPedido = p.numeroPedido;
            baseUrl = getBaseUrlPorTipo(p.nomeTipoPedido);

            holder.txtIdPedido.setText("Pedido #" + p.numeroPedido);
            holder.txtInfoPrincipal.setText("Favorecido: " + p.nomeFavorecido);
            holder.txtInfoSecundaria.setText("Tipo: " + p.nomeTipoPedido);
        }

        final String finalUrl = baseUrl + "?idPedido=" + idPedido;
        final String pedidoParaPesquisa = idPedido;
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), WebViewActivity.class);
            intent.putExtra("URL", finalUrl);
            intent.putExtra("TOKEN", token);
            intent.putExtra("NUM_PEDIDO", pedidoParaPesquisa);
            v.getContext().startActivity(intent);
        });
    }

    private String getBaseUrlPorTipo(String tipo) {
        if (tipo == null) return "";
        String t = tipo.toLowerCase();
        if (t.contains("adiantamento") && !t.contains("acerto")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoAdiantamento.aspx";
        if (t.contains("acerto")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoAcertoAdiantamento.aspx";
        if (t.contains("reembolso")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoReembolso.aspx";
        if (t.contains("diária") || t.contains("diaria")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoDiaria.aspx";
        if (t.contains("bolsa") && !t.contains("lote")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoBolsa.aspx";
        if (t.contains("jurídica")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoPessoaJuridica.aspx";
        if (t.contains("física")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoPessoaFisica.aspx";
        if (t.contains("receita")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/EntradaDeReceita.aspx";
        if (t.contains("transferência")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoTransferencia.aspx";
        if (t.contains("parecer")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoParecerTecnico.aspx";
        if (t.contains("lote")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoBolsaLote.aspx";
        if (t.contains("contratação")) return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoContratacao.aspx";
        return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoCompra.aspx";
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtIdPedido, txtInfoPrincipal, txtInfoSecundaria;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtIdPedido = itemView.findViewById(R.id.txt_id_pedido);
            txtInfoPrincipal = itemView.findViewById(R.id.txt_info_principal);
            txtInfoSecundaria = itemView.findViewById(R.id.txt_info_secundaria);
        }
    }
}
