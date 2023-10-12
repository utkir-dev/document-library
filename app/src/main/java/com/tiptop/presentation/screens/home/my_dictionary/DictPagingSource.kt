package com.tiptop.presentation.screens.home.my_dictionary

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.repository.local.DaoAruzUser
import com.tiptop.data.repository.local.DaoUzarUser
import com.tiptop.presentation.screens.document_view.pdf.Dictionary
import kotlinx.coroutines.delay

class DictPagingSource(
    private val dao: DaoAruzUser
) : PagingSource<Int, Dictionary>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Dictionary> {
        val page = params.key ?: 0

        return try {
            val entities =
                dao.getPagedList(params.loadSize, page * params.loadSize) .sortedBy { it.date }

            // simulate page loading
          //  if (page != 0) delay(1000)
            LoadResult.Page(
                data = entities,
//                prevKey = if (page == 0) null else page - 1,
//                nextKey = if (entities.isEmpty()) null else page + 1
                prevKey = if (entities.isNotEmpty()) page + 1 else null,
                nextKey = if (page > 1) page - 1 else null
            )


        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Dictionary>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}