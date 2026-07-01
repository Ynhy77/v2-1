package com.example.data

import kotlinx.coroutines.flow.Flow

class ScanRepository(private val scanDao: ScanDao) {
    val allScans: Flow<List<ScanEntity>> = scanDao.getAllScans()

    suspend fun insertScan(scan: ScanEntity) {
        scanDao.insertScan(scan)
    }

    suspend fun clearAllScans() {
        scanDao.clearAllScans()
    }
}
