package com.example.listmeapp.auth.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.model.ClienteDTO

class ClientListAdapter(
    private var clients: List<ClienteDTO>,
    private val onItemClick: (ClienteDTO) -> Unit,    // Callback GERAL para clique no item
    private val onEditClick: (ClienteDTO) -> Unit,    // Callback para o BOTÃO de editar
    private val onDeleteClick: (ClienteDTO) -> Unit,  // Callback para o BOTÃO de deletar
    private val canEdit: Boolean,                     // Permissão para mostrar/usar botão de editar
    private val canDelete: Boolean,                   // Permissão para mostrar/usar botão de deletar
    private val isSelectMode: Boolean                 // Indica se a lista está em modo de seleção
) : RecyclerView.Adapter<ClientListAdapter.ClientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_client, parent, false)
        return ClientViewHolder(view, canEdit, canDelete, isSelectMode) // Passa isSelectMode
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(clients[position], onItemClick, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int = clients.size

    fun updateClients(newClients: List<ClienteDTO>) {
        this.clients = newClients
        notifyDataSetChanged() // Para simplicidade. Considere DiffUtil para melhor performance.
    }

    class ClientViewHolder(
        itemView: View,
        private val canEditView: Boolean, // Renomeado para evitar conflito com parâmetro do bind
        private val canDeleteView: Boolean,
        private val isSelectModeView: Boolean
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvClientName: TextView = itemView.findViewById(R.id.tvClientName)
        private val tvClientIdentifier: TextView = itemView.findViewById(R.id.tvClientIdentifier)
        private val tvClientPhone: TextView = itemView.findViewById(R.id.tvClientPhone)
        private val tvClientEmail: TextView = itemView.findViewById(R.id.tvClientEmail)
        private val btnEditClient: ImageButton = itemView.findViewById(R.id.btnEditClient)
        private val btnDeleteClient: ImageButton = itemView.findViewById(R.id.btnDeleteClient)

        fun bind(
            client: ClienteDTO,
            onItemClickCallback: (ClienteDTO) -> Unit, // Renomeado para clareza
            onEditCallback: (ClienteDTO) -> Unit,
            onDeleteCallback: (ClienteDTO) -> Unit
        ) {
            tvClientName.text = client.nome
            tvClientIdentifier.text = "CNPJ/CPF: ${client.cnpj ?: "N/A"}"
            tvClientPhone.text = "Telefone: ${client.telefone}"
            tvClientEmail.text = "Email: ${client.email}"

            if (isSelectModeView) {
                // Em modo de seleção, o clique no item inteiro aciona o callback
                itemView.setOnClickListener { onItemClickCallback(client) }
                btnEditClient.visibility = View.GONE
                btnDeleteClient.visibility = View.GONE
            } else {
                // Em modo normal, o clique no item inteiro pode não fazer nada (ou abrir detalhes no futuro)
                itemView.setOnClickListener(null) // Ou defina outra ação se desejar

                // Configura os botões de ação apenas se não estiver em modo de seleção
                if (canEditView) {
                    btnEditClient.visibility = View.VISIBLE
                    btnEditClient.setOnClickListener { onEditCallback(client) }
                } else {
                    btnEditClient.visibility = View.GONE
                }

                if (canDeleteView) {
                    btnDeleteClient.visibility = View.VISIBLE
                    btnDeleteClient.setOnClickListener { onDeleteCallback(client) }
                } else {
                    btnDeleteClient.visibility = View.GONE
                }
            }
        }
    }
}