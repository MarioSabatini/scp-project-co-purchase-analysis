# Progetto di Analisi di Co-Acquisto

Questo repository contiene il codice per l'analisi di co-acquisto di prodotti utilizzando Scala e Apache Spark su Google Cloud DataProc. Di seguito sono riportate le istruzioni per eseeguire il programma e replicare gli esperimenti fatti.

## Genrazione del bucket e caricamento dei file necesari

Si deve generare un bucket su GoogleCloud e si deve caricare il file cvs fornito nelle specifiche del progetto chiamato "order_products.csv", si deve poi caricare anche il jar presente nel repository che si trova nel path:
```bash
target/scala-2.12/co-purchase_analysis_2.12.jar
```

## Creazione di un Cluster DataProc

Per creare un cluster DataProc con un solo worker, utilizzare il seguente comando:

```bash
gcloud dataproc clusters create <cluster-name> \
    --single-node \
    --region <region> \
    --zone <zone>\
    --master-machine-type <machine>\
    --master-boot-disk-size <gb> \
    --image-version 2.2-debian12 \
    --project <project-name>
 ```
Per creare un cluster DataProc con n nodi, utilizzare il seguente comando:
```bash
gcloud dataproc clusters create <cluster-name> \
    --region <region> \
    --zone <zone>\
    --master-machine-type <machine>\
     --num-workers <n> \ 
     --worker-machine-type <worker-machine> \ 
     --worker-boot-disk-size <gb> \
    --master-boot-disk-size <gb> \
    --image-version 2.2-debian12 \
    --project <project-name> \
   ```
   
## Submit del Job

Per eseguire il submit di un job sul cluster utilizzare il seguente comando:
```bash
gcloud dataproc jobs submit spark \
  --cluster=<cluster_name> \
  --region=<region> \
  --properties=spark.executor.instances=<numExecutors>,spark.executor.memory=6g, spark.executor.cores=3, spark.driver.memory=4g \
  --jar=<jarPath> \
  -- <PathOfBucket>
    ```
Alla fine dell'esecuzione verrà creata una cartella nel bucket, specificato durante il submit del job, chiamata "results" dove dentro sarà presente un file chiamato "part-00000". Per visualizzare il file in csv si può scaricare il file e rinominarlo "part-00000.csv".
