package com.ecommerce.user.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationScrollListener(var layoutManager: LinearLayoutManager?) : RecyclerView.OnScrollListener() {

    abstract fun isLastPage(): Boolean

    abstract fun isLoading(): Boolean



    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        recyclerView.let { super.onScrolled(it, dx, dy) }

        val visibleItemCount = layoutManager?.childCount
        val totalItemCount = layoutManager?.itemCount
        val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition()

        if (!isLoading() && !isLastPage()) {
            if (visibleItemCount != null) {
                if (visibleItemCount + firstVisibleItemPosition!! >= totalItemCount!! && firstVisibleItemPosition >= 0) {
                    loadMoreItems()
                }
            }
        }
    }
    abstract fun loadMoreItems()
}