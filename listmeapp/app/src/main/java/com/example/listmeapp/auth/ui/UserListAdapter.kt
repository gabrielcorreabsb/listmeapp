package com.example.listmeapp.auth.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.listmeapp.R
import com.example.listmeapp.data.model.UsuarioResponse // Usaremos UsuarioResponse para ter mais dados se necessário

class UserListAdapter(
    private var users: List<UsuarioResponse>,
    private val onEditClick: (UsuarioResponse) -> Unit,
    private val onDeleteClick: (UsuarioResponse) -> Unit
) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<UsuarioResponse>) {
        this.users = newUsers
        notifyDataSetChanged() // Para simplicidade. Considere DiffUtil para melhor performance.
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvUserCargo: TextView = itemView.findViewById(R.id.tvUserCargo)
        private val btnEditUser: ImageButton = itemView.findViewById(R.id.btnEditUser)
        private val btnDeleteUser: ImageButton = itemView.findViewById(R.id.btnDeleteUser)

        fun bind(user: UsuarioResponse, onEdit: (UsuarioResponse) -> Unit, onDelete: (UsuarioResponse) -> Unit) {
            tvUserName.text = user.nome
            tvUserCargo.text = user.cargo.replace("_", " ").capitalizeWords() // Formata o cargo

            btnEditUser.setOnClickListener { onEdit(user) }
            btnDeleteUser.setOnClickListener { onDelete(user) }
        }

        // Helper para capitalizar (pode ser movido para um arquivo de extensões)
        private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) }
    }
}