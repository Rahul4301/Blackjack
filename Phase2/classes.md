# Blackjack Multiplayer Game - Complete Class Specifications

## Server Class
**Class Variables:**
- serverSocket: ServerSocket – Socket that listens for incoming client connections.
- port: int – Port number the server runs on.
- isRunning: boolean – Indicates if the server is currently active.
- activeTables: ArrayList\<GameTable> – List of all active game tables.
- registeredAccounts: HashMap\<String, Account> – Maps usernames to Account objects for authentication.
- clientSockets: ArrayList\<Socket> – List of all connected client sockets.
- clientThreads: ArrayList\<Thread> – List of threads handling each client connection.
- threadPool: ExecutorService – Thread pool for managing concurrent client connections.

**Methods:**
- Server(port: int): void – Constructor that initializes the server on the specified port.
- start(): void – Starts the server and begins accepting client connections.
- stop(): void – Stops the server and closes all client connections.
- acceptClients(): void – Continuously listens for and accepts new client connections.
- handleClient(socket: Socket): void – Spawns a new thread to handle communication with a specific client.
- authenticateUser(username: String, password: String): Account – Validates credentials and returns Account if valid.
- registerUser(username: String, password: String, accountType: String): boolean – Creates a new user account.
- createTable(tableID: String, dealer: Dealer): GameTable – Creates a new game table with the specified dealer.
- getAvailableTables(): ArrayList\<GameTable> – Returns list of tables that have available seats.
- addPlayerToTable(playerUsername: String, tableID: String): boolean – Adds player to specified table if space available.
- removePlayerFromTable(playerUsername: String, tableID: String): void – Removes player from the table.
- processPlayerAction(playerUsername: String, tableID: String, action: String, amount: double): void – Handles player game actions.
- processDealerAction(dealerUsername: String, tableID: String, action: String): void – Handles dealer game actions.
- broadcastToTable(tableID: String, message: String): void – Sends a message to all clients at the specified table.
- sendToClient(socket: Socket, object: Object): void – Sends a serialized object to a specific client.
- receiveFromClient(socket: Socket): Object – Receives and deserializes an object from a client.

---

## Client Class
**Class Variables:**
- socket: Socket – Socket connection to the server.
- serverAddress: String – IP address or hostname of the server.
- serverPort: int – Port number to connect to on the server.
- outputStream: ObjectOutputStream – Stream for sending objects to server.
- inputStream: ObjectInputStream – Stream for receiving objects from server.
- isConnected: boolean – Indicates if client is connected to server.
- currentUser: Account – The currently logged-in user account.
- gui: GUI – Reference to the graphical user interface.
- listenerThread: Thread – Thread that continuously listens for server messages.
- isListening: boolean – Controls the listener thread loop.

**Methods:**
- Client(serverAddress: String, serverPort: int): void – Constructor that initializes connection parameters.
- connect(): boolean – Establishes connection to the server.
- disconnect(): void – Closes connection to the server.
- login(username: String, password: String): boolean – Sends login request to server and processes response.
- register(username: String, password: String, accountType: String): boolean – Sends registration request to server.
- requestTableList(): ArrayList\<GameTable> – Requests and returns list of available tables from server.
- joinTable(tableID: String): boolean – Requests to join a specific table.
- leaveTable(): void – Requests to leave the current table.
- sendPlayerAction(action: String, amount: double): void – Sends a player game action to the server.
- sendDealerAction(action: String): void – Sends a dealer game action to the server.
- sendToServer(object: Object): void – Serializes and sends an object to the server.
- receiveFromServer(): Object – Receives and deserializes an object from the server.
- startListening(): void – Starts the listener thread to receive server updates.
- stopListening(): void – Stops the listener thread.
- updateGUI(gameState: GameState): void – Updates the GUI with new game state from server.

---

## Account Class
**Implements:** Serializable

**Class Variables:**
- username: String – User's unique username.
- password: String – User's password (should be hashed in production).
- accountType: String – Type of account ("Player" or "Dealer").
- sessionID: String – Unique identifier for the current session.
- isOnline: boolean – Indicates if the user is currently online.

**Methods:**
- Account(username: String, password: String, accountType: String): void – Constructor that initializes a new account.
- getUsername(): String – Returns the username.
- getPassword(): String – Returns the password.
- getAccountType(): String – Returns the account type.
- setSessionID(sessionID: String): void – Sets the session identifier.
- setOnlineStatus(status: boolean): void – Updates the online status.

---

## Player Class (extends Account)
**Implements:** Serializable

**Class Variables:**
- balance: double – Player's current account balance.
- hand: Hand – Player's current hand of cards.
- currentBet: double – Amount wagered in the current round.
- isActive: boolean – Indicates whether player is still in the current round.
- tableID: String – ID of the table the player is currently at.

**Methods:**
- Player(username: String, password: String, balance: double): void – Constructor that initializes a player with starting balance.
- placeBet(amount: double): boolean – Validates and places a bet if balance is sufficient.
- hit(): void – Signals that the player wants another card.
- stand(): void – Marks player as finished taking actions for this round.
- doubleDown(): void – Doubles the bet and draws one final card.
- split(): void – Splits a hand if both cards are equal in rank.
- updateBalance(amount: double): void – Adds or subtracts funds based on game outcome.
- getHandValue(): int – Returns the numerical value of the player's hand.
- isBust(): boolean – Returns true if the player's hand exceeds 21.
- resetForNewRound(): void – Clears hand and bet for a new round.

---

## Dealer Class (extends Account)
**Implements:** Serializable

**Class Variables:**
- dealerID: String – Unique identifier for the dealer.
- hand: Hand – The dealer's hand of cards.
- managedTableID: String – ID of the table this dealer manages.

**Methods:**
- Dealer(username: String, password: String): void – Constructor that initializes a new dealer.
- dealCard(player: Player, deck: Deck): void – Deals one card from the deck to the target player.
- playTurn(deck: Deck): void – Dealer automatically hits until reaching a minimum hand value of 17.
- mustHit(): boolean – Returns true if dealer must hit (hand value < 17).
- mustStand(): boolean – Returns true if dealer must stand (hand value >= 17).
- revealHiddenCard(): void – Reveals the dealer's face-down card to players.
- resetHand(): void – Clears dealer's hand after round completion.
- setManagedTable(tableID: String): void – Assigns a table to this dealer.

---

## GameTable Class
**Implements:** Serializable

**Class Variables:**
- tableID: String – Unique identifier for the game table.
- dealer: Dealer – Dealer assigned to manage this table.
- players: ArrayList\<Player> – List of all active players at this table.
- deck: Deck – Deck or shoe used for card dealing.
- maxPlayers: int – Maximum number of players allowed at this table.
- minBet: double – Minimum bet amount for this table.
- maxBet: double – Maximum bet amount for this table.
- gameState: String – Current state of the game ("WAITING", "BETTING", "DEALING", "PLAYER_TURN", "DEALER_TURN", "RESULTS").
- currentPlayerIndex: int – Index of the player whose turn it is.

**Methods:**
- GameTable(tableID: String, dealer: Dealer, maxPlayers: int): void – Constructor that initializes the table with a dealer.
- addPlayer(player: Player): boolean – Adds a player to the table if capacity allows.
- removePlayer(player: Player): void – Removes a player from the table.
- startRound(): void – Begins a new round of play.
- dealInitialCards(): void – Deals two cards to each player and dealer.
- processPlayerTurn(player: Player, action: String): void – Processes a player's action during their turn.
- processDealerTurn(): void – Executes the dealer's turn according to house rules.
- evaluateHands(): void – Compares dealer's and players' hands and determines winners.
- payoutWinners(): void – Updates player balances based on win/loss outcomes.
- resetTable(): void – Clears bets and hands to prepare for a new round.
- getGameState(): String – Returns the current game phase.
- isFull(): boolean – Returns true if the table is at maximum capacity.

---

## GUI Class
**Class Variables:**
- client: Client – Reference to the client for server communication.
- currentScreen: String – Tracks the currently active display screen.
- playerDashboard: JPanel – Specific UI panel for player-only screens.
- dealerDashboard: JPanel – Specific UI panel for dealer-only screens.
- gameState: GameState – Current game state received from server.

**Methods:**
- GUI(client: Client): void – Constructor that initializes the GUI with a client reference.
- displayLoginScreen(): void – Shows username/password fields and login button.
- displayRegisterScreen(): void – Shows registration form for new users.
- displayLobby(tables: ArrayList\<GameTable>): void – Shows available tables and waitlist if tables are full.
- displayTable(gameState: GameState): void – Renders game table with cards, chips, and players.
- displayPlayerDashboard(player: Player): void – Displays player-specific information and controls.
- displayDealerDashboard(dealer: Dealer): void – Displays dealer-specific controls and information.
- showPlayerActions(actions: ArrayList\<String>): void – Displays available player actions (hit, stand, etc.).
- showDealerActions(actions: ArrayList\<String>): void – Displays dealer controls (deal, shuffle, manage).
- updateDisplay(gameState: GameState): void – Refreshes display with current game data from server.
- showMessage(message: String): void – Displays a message to the user.
- getPlayerInput(prompt: String): String – Prompts user for input and returns their response.

---

## Card Class
**Implements:** Serializable

**Class Variables:**
- rank: String – The card's rank (2–10, J, Q, K, A).
- suit: String – The suit of the card (Hearts, Diamonds, Clubs, Spades).
- value: int – The numeric value of the card in Blackjack.

**Methods:**
- Card(rank: String, suit: String): void – Constructor that creates a card object.
- getRank(): String – Returns the rank of the card.
- getSuit(): String – Returns the suit of the card.
- getValue(): int – Returns the numeric Blackjack value.
- toString(): String – Returns a formatted card description (e.g., "Ace of Spades").

---

## Deck Class
**Implements:** Serializable

**Class Variables:**
- cards: ArrayList\<Card> – List of all cards currently in the deck or shoe.
- numDecks: int – Number of decks combined in the shoe (default: 3).

**Methods:**
- Deck(numDecks: int): void – Constructor that creates and shuffles the deck.
- shuffle(): void – Randomly shuffles the order of cards.
- dealCard(): Card – Deals the top card and removes it from the deck.
- cardsRemaining(): int – Returns the number of undealt cards left.
- resetDeck(): void – Rebuilds and reshuffles the deck once depleted or halfway used.

---

## Hand Class
**Implements:** Serializable

**Class Variables:**
- cards: ArrayList\<Card> – List of all cards currently in the hand.
- isActive: boolean – Indicates whether this hand is still in play (has not busted or stood).

**Methods:**
- Hand(): void – Constructor that initializes an empty hand.
- addCard(card: Card): void – Adds a new card to the hand.
- getValue(): int – Calculates and returns the total value of the hand (handles Aces as 1 or 11).
- isBust(): boolean – Returns true if the hand value exceeds 21.
- isBlackjack(): boolean – Returns true if the hand is a natural Blackjack (Ace + 10-value card).
- clearHand(): void – Empties all cards to prepare for a new round.
- getCards(): ArrayList\<Card> – Returns the list of cards in the hand.
- toString(): String – Returns a formatted description of all cards in the hand.

---

## GameState Class
**Implements:** Serializable

**Class Variables:**
- tableID: String – Identifier for the table this state represents.
- dealerHand: Hand – The dealer's current hand.
- playerHands: HashMap\<String, Hand> – Maps player usernames to their hands.
- playerBets: HashMap\<String, Double> – Maps player usernames to their current bets.
- playerBalances: HashMap\<String, Double> – Maps player usernames to their balances.
- currentTurn: String – Username of the player whose turn it is.
- gamePhase: String – Current phase of the game ("WAITING", "BETTING", "DEALING", "PLAYER_TURN", "DEALER_TURN", "RESULTS").
- dealerCardHidden: boolean – Indicates if the dealer's second card is hidden.
- availableActions: ArrayList\<String> – List of actions available to the current player.
- message: String – Message to display to players (e.g., "John's turn", "Dealer busts!").

**Methods:**
- GameState(tableID: String): void – Constructor that initializes a game state for a table.
- setDealerHand(hand: Hand, hideCard: boolean): void – Sets the dealer's hand and whether to hide a card.
- addPlayerData(username: String, hand: Hand, bet: double, balance: double): void – Adds a player's information to the state.
- setCurrentTurn(username: String): void – Sets which player's turn it is.
- setGamePhase(phase: String): void – Sets the current game phase.
- setAvailableActions(actions: ArrayList\<String>): void – Sets the actions available to the current player.
- setMessage(message: String): void – Sets a message to display to players.
- toString(): String – Returns a string representation of the game state.

---

## Message Class
**Implements:** Serializable

**Class Variables:**
- messageType: String – Type of message ("LOGIN", "REGISTER", "PLAYER_ACTION", "DEALER_ACTION", "JOIN_TABLE", etc.).
- senderUsername: String – Username of the sender.
- data: HashMap\<String, Object> – Flexible key-value storage for message data.
- timestamp: long – Time the message was created.

**Methods:**
- Message(type: String, sender: String): void – Constructor that creates a message with specified type and sender.
- addData(key: String, value: Object): void – Adds a key-value pair to the message data.
- getData(key: String): Object – Retrieves a value from the message data.
- getMessageType(): String – Returns the type of message.
- getSender(): String – Returns the username of the sender.
- toString(): String – Returns a string representation of the message.