const canvas = document.getElementById("gameCanvas");
const ctx = canvas.getContext("2d");
const gridSize = 8;
const squareSize = canvas.width / gridSize;

let color = "#" + Math.floor(Math.random()*16777215).toString(16);
let isDrawing = false;

for (let i = 0; i < gridSize; i++) {
  for (let j = 0; j < gridSize; j++) {
    ctx.strokeRect(i * squareSize, j * squareSize, squareSize, squareSize);
  }
}

canvas.addEventListener("mousedown", e => {
  isDrawing = true;
  ctx.fillStyle = color;
});

canvas.addEventListener("mouseup", e => {
  isDrawing = false;
  // TODO: send square coordinates to Java server via WebSocket or Socket bridge
});

canvas.addEventListener("mousemove", e => {
  if (isDrawing) {
    const x = e.offsetX;
    const y = e.offsetY;
    ctx.fillRect(x, y, 4, 4);
  }
});
