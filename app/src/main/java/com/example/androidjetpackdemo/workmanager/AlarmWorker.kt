package com.example.androidjetpackdemo.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class AlarmWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        const val TAG = "Work Manager"
    }

    init {
        Log.d(TAG, "AlarmWorker init")
    }

    override fun doWork(): Result {

        Log.d(TAG, "AlarmWorker doWork")

        // 接受 WorkRequest 发过来的信息
        var inputString = inputData.getString(WorkManagerActivity.WORK_REQUEST_DATA_KEY)
        // 向 WorkManager 传递数据
        var outputData = Data.Builder().putString(WorkManagerActivity.WORKER_DATA_KEY,"$inputString , I am working").build()
        /*
         * 执行成功返回Result.success()
         * 执行失败返回Result.failure()
         * 需要重新执行返回Result.retry()
         */
        return Result.success(outputData)
    }

}