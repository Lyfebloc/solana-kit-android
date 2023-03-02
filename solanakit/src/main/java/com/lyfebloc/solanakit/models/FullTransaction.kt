package com.lyfebloc.solanakit.models

data class FullTransaction(
    val transaction: Transaction,
    val tokenTransfers: List<FullTokenTransfer>
)

data class FullTokenTransfer(
    val tokenTransfer: TokenTransfer,
    val mintAccount: MintAccount
)
