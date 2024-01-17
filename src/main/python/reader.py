import serial.tools.list_ports
import mysql.connector

print("Started python executable")

ports = serial.tools.list_ports.comports(3)
portList = [str(onePort) for onePort in ports]

# val = input("Select Port: COM")
val = 4

portVar = None
for x in range(0, len(portList)):
    if portList[x].startswith("COM" + str(val)):
        portVar = "COM" + str(val)

if portVar is None:
    print("Selected port not found. Exiting.")
    exit()

# Connect to MySQL database
db_user = "root"
db_password = "pluto"
db_host = "localhost"
db_name = "GoGreen"

conn = mysql.connector.connect(
    host=db_host,
    user=db_user,
    password=db_password,
    database=db_name
)

cursor = conn.cursor()

serialInst = serial.Serial()
serialInst.baudrate = 9600
serialInst.port = portVar
serialInst.open()

while True:
    if serialInst.in_waiting:
        packet = serialInst.readline().decode('utf').rstrip('\n')
        print(packet)
        # Insert data into the MySQL database
        cursor.execute('INSERT INTO sensor_data (measurement_time, sensor_value) VALUES (NOW(), %s)', (packet,))
        conn.commit()

print("Ended python executable")
