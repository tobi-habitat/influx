using System;
using System.Linq;
using System.Net;
using System.Threading;
using System.Threading.Tasks;
using Influx;
using MoreLinq;

namespace App
{
    class Program
    {
        private const string metric = "metric.dotnet";
        private const int influxBatchSize = 5000;
        private const int recordCount = 10 * 1000 * 1000;
        
        private const long TicksFaktorForSeconds = 10 * 1000 * 1000;
        

        static void Main(string[] args)
        {
            var influx = new InfluxService("http://localhost:8086", "test");
            influx.CreateDatabase().Wait();

            var now = DateTime.Now;

            Enumerable.Range(0, recordCount)
                .Select(i => CreateInfluxRow(new DateTime(now.Ticks + i * TicksFaktorForSeconds), 10.001 + ""))
                .Batch(influxBatchSize)
                .ForEach(async enumerable =>
                {
                    var messages = enumerable as string[] ?? enumerable.ToArray();
                    var response = await influx.WriteBulk(messages);

                    if (response.StatusCode != HttpStatusCode.NoContent)
                    {
                        Console.WriteLine(await response.Content.ReadAsStringAsync());
                    }
                });
        }
        
        private static string CreateInfluxRow(DateTime timestampFrom, string value)
        {
            value = value.Replace(",", ".");
        
            //+"00" because *100 memory overflow
            //ticks are in 100 nanoseconds accuracy
            var ticksInNanoSeconds = timestampFrom.Ticks + "00";
            var precision = ticksInNanoSeconds.Substring(ticksInNanoSeconds.Length - 6);
            var epocheTimeInNanoSeconds = new DateTimeOffset(timestampFrom).ToUnixTimeMilliseconds() + precision;
            var influxStructure = string.Format("{0} value={1} {2}", metric, value, epocheTimeInNanoSeconds);
            return influxStructure;
        }
    }
}