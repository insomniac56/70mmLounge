package com.example.data.model

enum class ReportTimeRange(val label: String) {
    TODAY("Today (Daily)"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

data class GstSlabSummary(
    val rate: Double,
    val taxableAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val totalTax: Double
)

data class CategorySaleStat(
    val category: String,
    val itemCount: Int,
    val totalRevenue: Double,
    val percentageOfSales: Double
)

data class TopSellingProduct(
    val productName: String,
    val category: String,
    val quantitySold: Int,
    val totalRevenue: Double
)

data class PaymentMethodBreakdown(
    val cashTotal: Double,
    val cardTotal: Double,
    val upiTotal: Double,
    val cashCount: Int,
    val cardCount: Int,
    val upiCount: Int
)

data class SalesSummary(
    val timeRange: ReportTimeRange,
    val orderCount: Int,
    val grossSales: Double,
    val totalDiscounts: Double,
    val totalGst: Double,
    val totalCostOfGoods: Double,
    val netProfit: Double,
    val averageOrderValue: Double,
    val gstSlabs: List<GstSlabSummary>,
    val categoryBreakdown: List<CategorySaleStat>,
    val topSellingProducts: List<TopSellingProduct>,
    val paymentBreakdown: PaymentMethodBreakdown,
    val orders: List<OrderWithItems>
)
