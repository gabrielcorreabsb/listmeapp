package com.example.listmeapp.auth.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.model.ItemOrcamentoResponseDTO // DTO para os itens
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class BudgetDetailItemsAdapter(
    private var items: List<ItemOrcamentoResponseDTO>
) : RecyclerView.Adapter<BudgetDetailItemsAdapter.DetailItemViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget_product_detail, parent, false)
        return DetailItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ItemOrcamentoResponseDTO>) {
        this.items = newItems
        notifyDataSetChanged() // Para simplicidade. Considere DiffUtil para listas grandes.
    }

    inner class DetailItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvDetailItemProductName)
        private val tvPriceQuantity: TextView = itemView.findViewById(R.id.tvDetailItemPriceQuantity)

        fun bind(item: ItemOrcamentoResponseDTO) {
            tvProductName.text = item.nomeProduto ?: "Produto não especificado"

            val quantity = item.quantidade
            val unitPrice = item.precoUnitario ?: BigDecimal.ZERO
            val subtotal = item.valorTotalItem ?: BigDecimal.ZERO
            val unit = item.unidadeMedidaProduto ?: "UN"

            // Formata a string de detalhes: Qtd: X UN x R$ Y,YY = R$ Z,ZZ
            val priceDetailsText = "Qtd: $quantity ${unit.lowercase()} x ${currencyFormat.format(unitPrice)} = ${currencyFormat.format(subtotal)}"
            tvPriceQuantity.text = priceDetailsText
        }
    }
}