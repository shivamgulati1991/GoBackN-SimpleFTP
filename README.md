# GoBackN-SimpleFTP

Implement a Simple FTP connection with Go Back N protocol.

### The Simple-FTP Client (Sender)

The Simple-FTP client implements the sender in the reliable data transfer. When the client starts, it reads data from a
file specified in the command line, and calls rdt_send() to transfer the data to the Simple-FTP server. For this
project, we will assume that rdt_send() provides data from the file on a byte basis. The client also implements the
sending side of the reliable Go-back-N protocol, receiving data from rdt_send(), buffering the data locally, and
ensuring that the data is received correctly at the server. The client also reads the value of the maximum segment
size (MSS) from the command line. The Go-back-N buffers the data it receives from rdt_send() until it has at
least one MSS worth of bytes. At that time it forms a segment that includes a header and MSS bytes of data; as a
result, all segments sent, except possibly for the very last one, will have exactly MSS bytes of data.
The client also reads the window size N from the command line, and implements the sending size of the Go-back-N
protocol. Specifically, if less than N segments are outstanding (i.e., have not been ACKed), it transmits the newly
formed segment to the server in a UDP packet. Otherwise, it buffers the segment and waits until the window has
advanced to transmit it. Note that if N = 1, the protocol reduces to Stop-and-Wait.

The header of the segment contains three fields:
• a 32-bit sequence number,
• a 16-bit checksum of the data part, computed in the same way as the UDP checksum, and
• a 16-bit field that has the value 0101010101010101, indicating that this is a data packet.

The client implements the full Go-back-N protocol as described in the book, including setting the timeout counter,
processing ACK packets (discussed shortly), advancing the window, and retransmitting packets as necessary 
2/3

### The Simple-FTP Server (Receiver)

The server listens on the well-known port 7735. It implements the receive side of the Go-back-N protocol, as
described in the book. Specifically, when it receives a data packet, it computes the checksum and checks whether it
is in-sequence, and if so, it sends an ACK segment (using UDP) to the client; it then writes the received data into a
file whose name is provided in the command line. If the packet received is out-of-sequence, or the checksum is
incorrect, it does nothing.
The ACK segment consists of three fields and no data:
• the 32-bit sequence number that is being ACKed,
• a 16-bit field that is all zeroes, and
• a 16-bit field that has the value 1010101010101010, indicating that this is an ACK packet.

### Generating Errors

Despite the fact that UDP is unreliable, the Internet does not in general lose packets. Therefore, we need a
systematic way of generating lost packet so as to test that the Go-back-N protocol works correctly (and to obtain
performance measurements, as will be explained shortly).

To this end, you will implement a probabilistic loss service at the server (receiver). Specifically, the server will read
the probability value p, 0 < p < 1 from the command line, representing the probability that a packet is lost. Upon
receiving a data packet, and before executing the Go-back-N protocol, the server will generate a random number r in
(0, 1). If r ≤ p, then this received packet is discarded and no other action is taken; otherwise, the packet is accepted
and processed according to the Go-back-N rules.

### To Run

The Simple-FTP server must be invoked as follows:

Simple_ftp_server port# file-name p

where port# is the port number to which the server is listening (for this project, this port number is always 7735),
file-name is the name of the file where the data will be written, and p is the packet loss probability discussed
above.
The Simple-FTP client must be invoked as follows:

Simple_ftp_server server-host-name server-port# file-name N MSS

where server-host-name is the host name where the server runs, server-port# is the port number of the server
(i.e., 7735), file-name is the name of the file to be transferred, N is the window size, and MSS is the maximum
segment size.
