using System.Collections.Generic;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace Influx
{
    public class InfluxService
    {
        private readonly string _endpoint;
        private readonly string _dbName;
        private readonly HttpClient _client;

        public InfluxService(string endpoint, string dbName)
        {
            _endpoint = endpoint;
            _dbName = dbName;
            _client = new HttpClient();
        }

        public async Task<HttpResponseMessage> CreateDatabase()
        {
            using(var content = new FormUrlEncodedContent(new[]{new KeyValuePair<string, string>("q", "CREATE DATABASE " + _dbName)})) 
            {
                return await _client.PostAsync(_endpoint + "/query", content);
            }
        }

        public async Task<HttpResponseMessage> Write(string message)
        {
            using (var content = new StringContent(message, Encoding.UTF8))
            {
                return await _client.PostAsync($"{_endpoint}/write?db={_dbName}", content);
            }  
        }
        
        public async Task<HttpResponseMessage> WriteBulk(IEnumerable<string> messages)
        {
            var message = string.Join("\n", messages);
            using (var content = new StringContent(message, Encoding.UTF8))
            {
                return await _client.PostAsync($"{_endpoint}/write?db={_dbName}", content);
            }
        }
    }
}