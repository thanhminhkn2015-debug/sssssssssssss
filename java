// Canvas setup
const canvas = document.getElementById('pongCanvas');
const ctx = canvas.getContext('2d');

// Game objects
const paddleWidth = 10;
const paddleHeight = 100;
const ballSize = 8;
const ballSpeed = 5;

let gameRunning = false;

// Player paddle (left)
const player = {
    x: 10,
    y: canvas.height / 2 - paddleHeight / 2,
    width: paddleWidth,
    height: paddleHeight,
    dy: 0,
    speed: 6,
    score: 0
};

// Computer paddle (right)
const computer = {
    x: canvas.width - paddleWidth - 10,
    y: canvas.height / 2 - paddleHeight / 2,
    width: paddleWidth,
    height: paddleHeight,
    dy: 0,
    speed: 4.5,
    score: 0
};

// Ball
const ball = {
    x: canvas.width / 2,
    y: canvas.height / 2,
    size: ballSize,
    dx: ballSpeed,
    dy: ballSpeed,
    speed: ballSpeed
};

// Keyboard input
const keys = {};
window.addEventListener('keydown', (e) => {
    keys[e.key] = true;
});

window.addEventListener('keyup', (e) => {
    keys[e.key] = false;
});

// Mouse input
canvas.addEventListener('mousemove', (e) => {
    const rect = canvas.getBoundingClientRect();
    const mouseY = e.clientY - rect.top;
    
    // Calculate distance to move towards mouse
    const playerCenter = player.y + player.height / 2;
    const distanceToMouse = mouseY - playerCenter;
    
    // Move towards mouse with a smooth acceleration
    if (Math.abs(distanceToMouse) > 10) {
        player.dy = (distanceToMouse > 0 ? 1 : -1) * player.speed;
    } else {
        player.dy *= 0.9; // Smooth deceleration
    }
});

// Handle arrow keys
function handlePlayerInput() {
    if (keys['ArrowUp'] || keys['w'] || keys['W']) {
        player.dy = -player.speed;
    } else if (keys['ArrowDown'] || keys['s'] || keys['S']) {
        player.dy = player.speed;
    } else if (!canvas.hasMouseListener) {
        player.dy *= 0.9; // Smooth deceleration when no input
    }
}

// Update player position
function updatePlayer() {
    handlePlayerInput();
    
    player.y += player.dy;
    
    // Boundary collision for player
    if (player.y < 0) {
        player.y = 0;
    }
    if (player.y + player.height > canvas.height) {
        player.y = canvas.height - player.height;
    }
}

// AI for computer paddle
function updateComputer() {
    const computerCenter = computer.y + computer.height / 2;
    const distance = ball.y - computerCenter;
    
    // AI tries to hit the ball
    if (Math.abs(distance) > 35) {
        computer.dy = (distance > 0 ? 1 : -1) * computer.speed;
    } else {
        computer.dy *= 0.9; // Smooth deceleration
    }
    
    computer.y += computer.dy;
    
    // Boundary collision for computer
    if (computer.y < 0) {
        computer.y = 0;
    }
    if (computer.y + computer.height > canvas.height) {
        computer.y = canvas.height - computer.height;
    }
}

// Update ball position
function updateBall() {
    ball.x += ball.dx;
    ball.y += ball.dy;
    
    // Top and bottom collision
    if (ball.y - ball.size < 0 || ball.y + ball.size > canvas.height) {
        ball.dy = -ball.dy;
        
        // Keep ball in bounds
        if (ball.y - ball.size < 0) {
            ball.y = ball.size;
        }
        if (ball.y + ball.size > canvas.height) {
            ball.y = canvas.height - ball.size;
        }
    }
    
    // Paddle collision
    // Player paddle collision
    if (
        ball.x - ball.size < player.x + player.width &&
        ball.y > player.y &&
        ball.y < player.y + player.height &&
        ball.dx < 0
    ) {
        ball.dx = -ball.dx;
        ball.x = player.x + player.width + ball.size;
        
        // Add spin based on where ball hit the paddle
        const hitPos = (ball.y - (player.y + player.height / 2)) / (player.height / 2);
        ball.dy += hitPos * 3;
    }
    
    // Computer paddle collision
    if (
        ball.x + ball.size > computer.x &&
        ball.y > computer.y &&
        ball.y < computer.y + computer.height &&
        ball.dx > 0
    ) {
        ball.dx = -ball.dx;
        ball.x = computer.x - ball.size;
        
        // Add spin based on where ball hit the paddle
        const hitPos = (ball.y - (computer.y + computer.height / 2)) / (computer.height / 2);
        ball.dy += hitPos * 3;
    }
    
    // Score points
    if (ball.x - ball.size < 0) {
        computer.score++;
        resetBall();
        updateScore();
    }
    
    if (ball.x + ball.size > canvas.width) {
        player.score++;
        resetBall();
        updateScore();
    }
}

// Reset ball to center
function resetBall() {
    ball.x = canvas.width / 2;
    ball.y = canvas.height / 2;
    ball.dx = (Math.random() > 0.5 ? 1 : -1) * ball.speed;
    ball.dy = (Math.random() - 0.5) * ball.speed;
}

// Draw game elements
function draw() {
    // Clear canvas with dark background
    ctx.fillStyle = '#1a1a1a';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Draw center line
    ctx.strokeStyle = '#fff';
    ctx.setLineDash([10, 10]);
    ctx.beginPath();
    ctx.moveTo(canvas.width / 2, 0);
    ctx.lineTo(canvas.width / 2, canvas.height);
    ctx.stroke();
    ctx.setLineDash([]);
    
    // Draw player paddle
    ctx.fillStyle = '#00ff88';
    ctx.shadowColor = '#00ff88';
    ctx.shadowBlur = 10;
    ctx.fillRect(player.x, player.y, player.width, player.height);
    ctx.shadowBlur = 0;
    
    // Draw computer paddle
    ctx.fillStyle = '#ff6b6b';
    ctx.shadowColor = '#ff6b6b';
    ctx.shadowBlur = 10;
    ctx.fillRect(computer.x, computer.y, computer.width, computer.height);
    ctx.shadowBlur = 0;
    
    // Draw ball
    ctx.fillStyle = '#ffff00';
    ctx.shadowColor = '#ffff00';
    ctx.shadowBlur = 15;
    ctx.beginPath();
    ctx.arc(ball.x, ball.y, ball.size, 0, Math.PI * 2);
    ctx.fill();
    ctx.shadowBlur = 0;
}

// Update score display
function updateScore() {
    document.getElementById('playerScore').textContent = player.score;
    document.getElementById('computerScore').textContent = computer.score;
}

// Game loop
function gameLoop() {
    if (gameRunning) {
        updatePlayer();
        updateComputer();
        updateBall();
        draw();
    }
    requestAnimationFrame(gameLoop);
}

// Start game
document.getElementById('startBtn').addEventListener('click', () => {
    gameRunning = !gameRunning;
    document.getElementById('startBtn').textContent = gameRunning ? 'Pause Game' : 'Start Game';
});

// Reset score
document.getElementById('resetBtn').addEventListener('click', () => {
    player.score = 0;
    computer.score = 0;
    resetBall();
    updateScore();
    gameRunning = false;
    document.getElementById('startBtn').textContent = 'Start Game';
});

// Initialize
resetBall();
updateScore();
gameLoop();
