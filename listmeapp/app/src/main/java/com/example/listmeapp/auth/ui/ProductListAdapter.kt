package com.example.listmeapp.auth.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listmeapp.R
import com.example.listmeapp.data.model.ProdutoDTO
import java.text.NumberFormat
import java.util.Locale

class ProductListAdapter(
    private var products: List<ProdutoDTO>,
    private val onItemClick: (ProdutoDTO) -> Unit,    // Callback GERAL para clique no item
    private val onEditClick: (ProdutoDTO) -> Unit,    // Callback para o BOTÃO de editar
    private val onDeleteClick: (ProdutoDTO) -> Unit,  // Callback para o BOTÃO de deletar
    private val isAdmin: Boolean,                     // Permissão para mostrar/usar botões
    private val isSelectMode: Boolean                 // Indica se a lista está em modo de seleção
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view, isAdmin, isSelectMode) // Passa isSelectMode
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position], onItemClick, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<ProdutoDTO>) {
        this.products = newProducts
        notifyDataSetChanged()
    }

    class ProductViewHolder(
        itemView: View,
        private val isAdminView: Boolean, // Renomeado para evitar conflito
        private val isSelectModeView: Boolean
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvProductUnit: TextView = itemView.findViewById(R.id.tvProductUnit)
        private val tvProductStatus: TextView = itemView.findViewById(R.id.tvProductStatus)
        private val btnEditProduct: ImageButton = itemView.findViewById(R.id.btnEditProduct)
        private val btnDeleteProduct: ImageButton = itemView.findViewById(R.id.btnDeleteProduct)
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        fun bind(
            product: ProdutoDTO,
            onItemClickCallback: (ProdutoDTO) -> Unit,
            onEditCallback: (ProdutoDTO) -> Unit,
            onDeleteCallback: (ProdutoDTO) -> Unit
        ) {
            tvProductName.text = product.nome
            tvProductPrice.text = currencyFormat.format(product.preco ?: 0.0) // Trata preco nulo
            tvProductUnit.text = product.unidadeMedida
            tvProductStatus.text = if (product.ativo == true) "Ativo" else "Inativo"

            Glide.with(itemView.context)
                .load(product.urlImagem)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(ivProductImage)

            if (isSelectModeView) {
                itemView.setOnClickListener { onItemClickCallback(product) }
                btnEditProduct.visibility = View.GONE
                btnDeleteProduct.visibility = View.GONE
            } else {
                itemView.setOnClickListener(null)
                if (isAdminView) { // Ações de Admin apenas se não for modo de seleção
                    btnEditProduct.visibility = View.VISIBLE
                    btnDeleteProduct.visibility = View.VISIBLE
                    btnEditProduct.setOnClickListener { onEditCallback(product) }
                    btnDeleteProduct.setOnClickListener { onDeleteCallback(product) }
                } else {
                    btnEditProduct.visibility = View.GONE
                    btnDeleteProduct.visibility = View.GONE
                }
            }
        }
    }
}