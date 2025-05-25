package com.example.listmeapp.auth.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.model.ItemOrcamentoResponseDTO // Usaremos este para display
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class BudgetItemsAdapter(
    private val items: MutableList<ItemOrcamentoResponseDTO>,
    private val onRemoveClick: (position: Int) -> Unit,
    private val onQuantityChange: (position: Int, newQuantity: Int) -> Unit
) : RecyclerView.Adapter<BudgetItemsAdapter.BudgetItemViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget_product, parent, false)
        return BudgetItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: ItemOrcamentoResponseDTO) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size) // Para atualizar posições subsequentes
        }
    }

    fun updateItemQuantity(position: Int, newQuantity: Int, newSubtotal: BigDecimal) {
        if (position >= 0 && position < items.size) {
            items[position] = items[position].copy(
                quantidade = newQuantity,
                valorTotalItem = newSubtotal
            )
            notifyItemChanged(position)
        }
    }

    inner class BudgetItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvBudgetItemProductName)
        private val tvPriceDetails: TextView = itemView.findViewById(R.id.tvBudgetItemPriceDetails)
        private val etQuantity: EditText = itemView.findViewById(R.id.etBudgetItemQuantity)
        private val tvSubtotal: TextView = itemView.findViewById(R.id.tvBudgetItemSubtotal)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveBudgetItem)
        private var currentTextWatcher: TextWatcher? = null


        fun bind(item: ItemOrcamentoResponseDTO) {
            tvProductName.text = item.nomeProduto ?: "Produto Desconhecido"
            val priceDetailText = "${item.quantidade} ${item.unidadeMedidaProduto ?: "UN"} x ${currencyFormat.format(item.precoUnitario ?: BigDecimal.ZERO)}"
            tvPriceDetails.text = priceDetailText
            tvSubtotal.text = currencyFormat.format(item.valorTotalItem ?: BigDecimal.ZERO)

            // Remover listener antigo antes de adicionar um novo para evitar múltiplos listeners
            currentTextWatcher?.let { etQuantity.removeTextChangedListener(it) }
            etQuantity.setText(item.quantidade.toString())

            currentTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val newQuantity = s.toString().toIntOrNull() ?: 0
                        if (items[adapterPosition].quantidade != newQuantity) { // Evitar loops infinitos
                            onQuantityChange(adapterPosition, newQuantity)
                        }
                    }
                }
            }
            etQuantity.addTextChangedListener(currentTextWatcher)

            btnRemove.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onRemoveClick(adapterPosition)
                }
            }
        }
    }
}