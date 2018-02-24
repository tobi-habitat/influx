using System;

namespace App
{
    public class Record
    {
        public readonly DateTime Time;
        public readonly double Value;

        public Record(DateTime time, double value)
        {
            Time = time;
            Value = value;
        }
    }
}