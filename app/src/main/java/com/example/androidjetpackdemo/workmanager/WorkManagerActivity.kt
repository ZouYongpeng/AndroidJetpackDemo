package com.example.androidjetpackdemo.workmanager

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.*
import com.example.androidjetpackdemo.R
import kotlinx.android.synthetic.main.activity_work_manager.*
import java.util.concurrent.TimeUnit

class WorkManagerActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Work Manager"
        const val WORK_REQUEST_DATA_KEY = "WORK_REQUEST_DATA_KEY"
        const val WORKER_DATA_KEY = "WORKER_DATA_KEY"
    }

    /**
     * Step 1 : 定义任务
     */
    private lateinit var alarmWorker: AlarmWorker

    private val workManager by lazy { WorkManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_manager)

        /**
         * Step 2 : 设置约束条件 constraints
         */
        var constraints = Constraints.Builder().apply {
            // true 表示充电时执行，默认为 false
            setRequiresCharging(true)
            // true 表示空闲时运行，默认为 false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setRequiresDeviceIdle(false)
            }
            // 设置在哪些网络状态下运行，NOT_REQUIRED表示不需要网络
            setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            // 设置是否尽在电量充足时运行，默认为 false
            setRequiresBatteryNotLow(true)
            // 设置是否尽在内存充足时运行，默认为 false
            setRequiresStorageNotLow(true)
        }.build()

        /**
         * Step 3 : 将 Constraints设置给 WorkRequest
         * OneTimeWorkRequest  ：一次性任务
         * PeriodicWorkRequest ：周期性任务
         */
        var oneTimeWorkRequest = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            // 设置约束条件
            .setConstraints(constraints)
            // 设置延迟10秒后执行
            .setInitialDelay(10, TimeUnit.SECONDS)
            // 假如Worker执行出现了异常，比如服务器宕机，
            // 那么可以在Worker的doWork()方法中返回Result.retry()，让Worker过一段时间重试，
            // 系统会有默认的指数退避策略，也可以自定义
//            .setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            // 设置 TAG 标签，后续可以通过 TAG 去跟踪或取消worker
            .addTag("AlarmOneTimeTag")
            // 可以通过setInputData()方法向Worker传递数据
            .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY,"one_time").build())
            .build()

        /**
         * Step 4 : WorkManager 实例把 WorkRequest 放入执行队列
         */
//        workManager.enqueue(oneTimeWorkRequest)

        /**
         * Step 5 : 通过 WorkInfo 获知任务的状态
         */
        var workInfoByRequestId = workManager.getWorkInfoById(oneTimeWorkRequest.id)
        var workInfoByRequestTag = workManager.cancelAllWorkByTag("AlarmOneTimeTag")
        var workInfoForUniqueWork = workManager.getWorkInfosForUniqueWork("")

        one_time_btn.setOnClickListener {
            Log.d(TAG, "click one_time_btn")
            val oneTimeWorkRequest = OneTimeWorkRequest
                .Builder(AlarmWorker::class.java)
                .setConstraints(constraints)
                .setInitialDelay(3, TimeUnit.SECONDS)
                .addTag("AlarmOneTimeTag")
                .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY,"one_time").build())
                .build()
            workManager.enqueue(oneTimeWorkRequest)
            workManager.observerWorkInfo(oneTimeWorkRequest)
        }

        periodic_time_btn.setOnClickListener {
            Log.d(TAG, "click periodic_time_btn")
            val periodicWorkRequest = PeriodicWorkRequest
                // 最小时间间隔是 15 分钟,如果设置了1秒，源码也会改为15分钟
                .Builder(AlarmWorker::class.java, 1, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .setInitialDelay(3, TimeUnit.SECONDS)
                .addTag("AlarmOneTimeTag")
                .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY, "periodic_time").build())
                .build()
            workManager.enqueue(periodicWorkRequest)
            workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id).observe(this, Observer {
                // 由于 periodicWorkRequest 会一直执行，不会收到 success data
                it?.apply {
                    Log.d(TAG, "workManager getData: state = $state , message = ${outputData.getString(WORKER_DATA_KEY)}")
                }
            })
        }

        order_btn.setOnClickListener {
            Log.d(TAG, "click order_btn")
            val firstWorkRequest = OneTimeWorkRequest
                .Builder(AlarmWorker::class.java)
                .setConstraints(constraints)
                .addTag("AlarmOneTimeTag")
                .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY,"first").build())
                .build()
            val secondWorkRequest = OneTimeWorkRequest
                .Builder(AlarmWorker::class.java)
                .setConstraints(constraints)
                .addTag("AlarmOneTimeTag")
                .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY,"second").build())
                .build()
            val thirdWorkRequest = OneTimeWorkRequest
                .Builder(AlarmWorker::class.java)
                .setConstraints(constraints)
                .addTag("AlarmOneTimeTag")
                .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY,"third").build())
                .build()
            // 1 -> 2 -> 3
//            workManager.beginWith(firstWorkRequest).then(secondWorkRequest).then(thirdWorkRequest).enqueue()
            // 1,2 -> 3
            workManager.beginWith(listOf(firstWorkRequest, secondWorkRequest)).then(thirdWorkRequest).enqueue()
            // 1 -> 2,3
            workManager.beginWith(firstWorkRequest).then(listOf(secondWorkRequest, thirdWorkRequest)).enqueue()

            workManager.observerWorkInfo(firstWorkRequest)
            workManager.observerWorkInfo(secondWorkRequest)
            workManager.observerWorkInfo(thirdWorkRequest)
        }

        cancel_all_worker_btn.setOnClickListener {
            Log.d(TAG, "cancel_all_worker_btn")
            workManager.cancelAllWork()
        }

    }

    private fun WorkManager.observerWorkInfo(workRequest: WorkRequest) {
        getWorkInfoByIdLiveData(workRequest.id).observe(this@WorkManagerActivity, Observer {
            it?.apply {
                Log.d(TAG, "workManager getData: state = $state , message = ${outputData.getString(WORKER_DATA_KEY)}")
            }
        })
    }

}