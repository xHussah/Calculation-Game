# Calculation Game - عجل بالحسبة

This project was completed in May 2025 as a school group assignment by a team of four students, including myself.

We developed a Java multiplayer game using a Server-Client architecture. When a client connects to the server, they are shown a login screen to enter their name. After logging in, players are directed to the players’ room, where the names of all connected players are displayed. A “Play” button at the bottom center of the screen allows players to move to the waiting room.

In the waiting room, players must wait for at least one other player to join before starting the game. Once another player connects, a 30-seconds countdown begins, and the “Start Game” button becomes active for any player to click. Players may choose to wait for more participants (up to 4). When the countdown ends, the game starts automatically, unless a 4th player joins earlier it begins immediately.

When the game starts, a 120-seconds timer begins, and questions appear on the screen. A player’s submission is only accepted if their answer is correct, after that a new question appears. Each round consists of 5 questions, and the player who answers all wins the game. If the timer runs out before anyone finishes, no winner is declared.

# To run the game:

1- Download the project files and open them in Apache NetBeans.

2- Run the Server first, then the Client.

3- If the server and client are on different devices, replace “localhost” in line 552 with the IP address of the server’s laptop.
