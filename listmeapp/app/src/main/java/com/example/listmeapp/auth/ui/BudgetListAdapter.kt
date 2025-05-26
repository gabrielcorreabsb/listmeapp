package com.example.listmeapp.auth.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat // Para cores
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.model.OrcamentoResponseDTO
import com.example.listmeapp.data.model.StatusOrcamento // Seu enum
import java.text.NumberFormat
import java.util.Locale
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException // Se precisar tratar
// e outros equivalentes de org.threeten.bp.*


class BudgetListAdapter(
    private var budgets: List<OrcamentoResponseDTO>,
    private val onItemClick: (OrcamentoResponseDTO) -> Unit
) : RecyclerView.Adapter<BudgetListAdapter.BudgetViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val outputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private val inputDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME // Padrão se o backend envia ISO

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgets[position], onItemClick)
    }

    override fun getItemCount(): Int = budgets.size

    fun updateBudgets(newBudgets: List<OrcamentoResponseDTO>) {
        this.budgets = newBudgets
        notifyDataSetChanged()
    }

    inner class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvClientName: TextView = itemView.findViewById(R.id.tvBudgetClientName)
        private val tvBudgetId: TextView = itemView.findViewById(R.id.tvBudgetId)
        private val tvBudgetDate: TextView = itemView.findViewById(R.id.tvBudgetDate)
        private val tvTotalValue: TextView = itemView.findViewById(R.id.tvBudgetTotalValue)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvBudgetStatus)

        fun bind(budget: OrcamentoResponseDTO, onItemClick: (OrcamentoResponseDTO) -> Unit) {
            tvClientName.text = "Cliente: ${budget.cliente.nome}"
            tvBudgetId.text = "ID: #${budget.id}"
            try {
                val parsedDate = LocalDateTime.parse(budget.dataOrcamento, inputDateFormatter)
                tvBudgetDate.text = "Data: ${outputDateFormatter.format(parsedDate)}"
            } catch (e: Exception) {
                tvBudgetDate.text = "Data: ${budget.dataOrcamento}" // Fallback se o parse falhar
            }
            tvTotalValue.text = "Total: ${currencyFormat.format(budget.valorTotal)}"
            tvStatus.text = budget.status.replace("_", " ").capitalizeWords()

            // Mudar a cor do background do status
            val statusColor = when (budget.status.uppercase()) {
                StatusOrcamento.PENDENTE.name -> ContextCompat.getColor(itemView.context, R.color.status_pending)
                StatusOrcamento.ENVIADO.name -> ContextCompat.getColor(itemView.context, R.color.status_sent)
                StatusOrcamento.APROVADO.name -> ContextCompat.getColor(itemView.context, R.color.status_approved)
                StatusOrcamento.REJEITADO.name -> ContextCompat.getColor(itemView.context, R.color.status_rejected)
                StatusOrcamento.CONCLUIDO.name -> ContextCompat.getColor(itemView.context, R.color.status_completed)
                StatusOrcamento.CANCELADO.name -> ContextCompat.getColor(itemView.context, R.color.status_cancelled)
                else -> Color.DKGRAY
            }
            tvStatus.setBackgroundColor(statusColor) // Ou use setBackgroundResource se criou drawables

            itemView.setOnClickListener { onItemClick(budget) }
        }

        private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) }
    }
}