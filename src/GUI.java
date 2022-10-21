import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class GUI extends JFrame {
	private JPanel contentPane;
	private JTextField tfInput;
	private JLabel lbResult;
	public static JTextArea textAreaHistory;
	public static JScrollPane areaScrollPane;

	public static char[] OPERATORLIST = { '+', '-', '/', '*', '^' };

	// Function for in fix calculating, input a string, output will be a double
	// array with size is 2,
	// FIrst element is the answer, and the second element is the flagged error
	public static double[] calculator(String inputString) {
		Stack<Double> operandStack = new Stack<Double>();
		Stack<Character> operatorStack = new Stack<Character>();
		boolean validInput = true;
		double[] resultAfterCalculate;
		double[] output = new double[] { 0, 0 };

		if (inputString == "") {
			validInput = false;
			output[1] = -1;
			return output;
		} else {
			char[] inputArr = inputString.replaceAll("\\s", "").toCharArray();
			// Check if the last element is operator, return syntax error
			if (contain(inputArr[inputArr.length - 1], OPERATORLIST)) {
				validInput = false;
				System.out.println("Syntax Error");
				output[1] = -1;
				return output;
			}
			char previousChar = inputArr[0];
			for (int i = 0; i < inputArr.length; i++) {
				String num = "";
				if (inputArr[i] == ' ') {
					continue;
				} else if (inputArr[i] >= '0' && inputArr[i] <= '9') {
					num += inputArr[i];
					while (i + 1 < inputArr.length
							&& ((inputArr[i + 1] >= '0' && inputArr[i + 1] <= '9') || (inputArr[i + 1] == '.'))) {
						num += inputArr[i + 1];
						i++;
					}
					if (isNumeric(num)) {
						if (operandStack.empty() && !operatorStack.empty() && operatorStack.peek() == '-') {
							operatorStack.pop();
							operandStack.push(-1 * Double.parseDouble(num));
						} else if (!operatorStack.empty() && operatorStack.peek() == '-') {
							operatorStack.pop();
							if (!operatorStack.empty() && previousChar == '(') {
								operandStack.push(-1 * Double.parseDouble(num));
							} else {
								operatorStack.push('-');
								operandStack.push(Double.parseDouble(num));
							}
						} else {
							operandStack.push(Double.parseDouble(num));
						}

					} else {
						validInput = false;
						System.out.println("Syntax Error");
						output[1] = -1;
						return output;
					}
					if (i > 0) {
						previousChar = inputArr[i];
					}

				} else if (inputArr[i] == '(') {
					operatorStack.push(inputArr[i]);
					if (i > 0) {
						previousChar = inputArr[i];
					}
				} else if (inputArr[i] == '-' && !operatorStack.isEmpty()
						&& (previousChar == '^' || previousChar == '*' || previousChar == '+' || previousChar == '/')) {

					// This case is really important to help people easy to write something likes
					// 18*-2 3^-2 or 7+-2
					// The calculator with know it as
					// 18*(-2) 3^(-2) or 7+(-2)
					num += inputArr[i];
					while (i + 1 < inputArr.length
							&& ((inputArr[i + 1] >= '0' && inputArr[i + 1] <= '9') || (inputArr[i + 1] == '.'))) {
						num += inputArr[i + 1];
						i++;
					}
					if (isNumeric(num)) {
						if (operandStack.empty() && !operatorStack.empty() && operatorStack.peek() == '-') {
							operatorStack.pop();
							operandStack.push(-1 * Double.parseDouble(num));
						} else if (!operatorStack.empty() && operatorStack.peek() == '-') {
							operatorStack.pop();
							if (!operatorStack.empty() && operatorStack.peek() == '(') {
								operandStack.push(-1 * Double.parseDouble(num));
							} else {
								operatorStack.push('-');
								operandStack.push(Double.parseDouble(num));
							}
						} else {
							operandStack.push(Double.parseDouble(num));
						}
					} else {
						validInput = false;
						System.out.println("Syntax Error");
						output[1] = -1;
						return output;
					}
					if (i > 0) {
						previousChar = inputArr[i];
					}
				} else if (inputArr[i] == ')') {
					if (validSizeOfStack(operandStack, 2)) {
						while (operatorStack.peek() != '(') {
							resultAfterCalculate = useOperator(operatorStack.pop(), operandStack.pop(),
									operandStack.pop());
							if (resultAfterCalculate[1] == 0) {
								operandStack.push(resultAfterCalculate[0]);
							} else {
								output[1] = checkErrorInResult(resultAfterCalculate);
								validInput = false;
								return output;
							}
						}
					} else if (validSizeOfStack(operandStack, 1)) {
						continue;
					} else {
						validInput = false;
						output[1] = -1;
						return output;
					}
					operatorStack.pop();
					if (i > 0) {
						previousChar = inputArr[i];
					}
				} else if (contain(inputArr[i], OPERATORLIST)) {
					while (!operatorStack.empty() && hasPrecedence(inputArr[i], operatorStack.peek())) {
						if (validSizeOfStack(operandStack, 2)) {
							resultAfterCalculate = useOperator(operatorStack.pop(), operandStack.pop(),
									operandStack.pop());
							if (resultAfterCalculate[1] == 0) {
								operandStack.push(resultAfterCalculate[0]);
							} else {
								output[1] = checkErrorInResult(resultAfterCalculate);
								validInput = false;
								return output;
							}
						} else {
							validInput = false;
							output[1] = -1;
							return output;
						}
					}
					operatorStack.push(inputArr[i]);
					if (i > 0) {
						previousChar = inputArr[i];
					}
				}

			}
			while (!operatorStack.empty()) {
				if (validSizeOfStack(operandStack, 2)) {
					resultAfterCalculate = useOperator(operatorStack.pop(), operandStack.pop(), operandStack.pop());
					if (resultAfterCalculate[1] == 0) {
						operandStack.push(resultAfterCalculate[0]);
					} else {
						output[1] = checkErrorInResult(resultAfterCalculate);
						validInput = false;
						return output;
					}
				} else if (validSizeOfStack(operandStack, 1)) {
					output[1] = 0;
					output[0] = operandStack.pop();
					return output;
				} else {
					validInput = false;
					output[1] = -1;
					return output;
				}
			}
			if (validInput == false) {
				output[1] = -1;
				return output;
			}

			if (operandStack.empty()) {
				output[1] = -1;
				return output;
			}
			output[0] = operandStack.pop();
			return output;
		}
	}

	// Function to check which the operator has precedence
	public static boolean hasPrecedence(char a, char b) {
		// Prioritize the calculations in brackets first. Then exponentiation =>
		// multiplication and division => and finally addition and subtraction.
		if (b == '(' || b == ')')
			return false;
		if ((a == '^') && (b == '*' || b == '/' || b == '+' || b == '-'))
			return false;
		if ((a == '*' || a == '/') && (b == '+' || b == '-'))
			return false;
		else
			return true;
	}

	// Function to check if a array contains the key data in it
	public static boolean contain(char key, char[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == key) {
				return true;
			}
		}
		return false;
	}

	// Function to flag error depend on the result
	public static double checkErrorInResult(double[] result) {
		if (result[1] == -1) {
			System.out.println("Syntax Error");
			return result[1];
		} else if (result[1] == -2) {
			System.out.println("Math Error");
			return result[1];
		} else {
			System.out.println("Power has to be an integer.");
			return result[1];
		}
	}

	// Function to check if the string is numeric (double or integer)
	public static boolean isNumeric(String input) {
		return input.matches("[-+]?\\d*\\.?\\d+");
	}

	// Function to check if size of stack is greater than the parameter
	public static boolean validSizeOfStack(Stack<Double> a, int size) {
		return (a.size() >= size);
	}

	// Function to calculation with given 2 operands with the operator
	public static double[] useOperator(char op, double a, double b) {
//		System.out.print("CALCULATING: " + b + " " + op + " " + a + " = ");

		double[] result = new double[] { 0, 0 };
		switch (op) {
		case '+':
			result[0] = b + a;
			System.out.println(result[0]);
			return result;
		case '-':
			result[0] = b - a;
			System.out.println(result[0]);
			return result;
		case '*':
			result[0] = b * a;
			System.out.println(result[0]);
			return result;
		case '/':
			if (a == 0) {
				result[1] = -2;
			} else {
				result[0] = b / a;
			}
			System.out.println(result[0]);
			return result;
		case '^':
			double c = 1;
			// the power have to be integer, not decimal.
			if (a % 1 == 0) {
				int j = (int) a;
				while (j != 0) {
					if (a < 0) {
						j += 1;
					} else {
						j -= 1;
					}
					c *= b;
				}
				if (a < 0) {
					c = 1 / c;
				}
				System.out.println(c);
				result[0] = c;
			} else {
				System.out.println("Please use integer for power of exponentiation.");
				result[1] = -3;
			}
			System.out.println(result[0]);
			return result;
		}
		return result;
	}

	// Function to return string depend on the flagged error. Return error or return
	// result
	public static String processOperation(String input) {
		double[] output = calculator(input);
		if (output[1] == -1) {
			return "SYNTAX ERROR     ";
		} else if (output[1] == -2) {
			return "MATH ERROR     ";
		} else if (output[1] == -3) {
			return "PLEASE USE INTEGER FOR POWER OF EXPONENTIATION     ";
		} else {
			textAreaHistory.setText(input + " = " + output[0] + "\n" + textAreaHistory.getText());
			textAreaHistory.setCaretPosition(0);
			return String.valueOf(output[0]) + " ";
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// Draw some decorations
	@Override
	public void paint(Graphics g) {
		super.paintComponents(g);
		int distanceX = 7;
		int distanceY = 30;

		int[] x = new int[3];
		int[] y = new int[3];

		g.setColor(new Color(79, 216, 255));
		x[0] = 359 + distanceX;
		x[1] = 426 + distanceX;
		x[2] = 426 + distanceX;
		y[0] = 257 + distanceY;
		y[1] = 222 + distanceY;
		y[2] = 257 + distanceY;
		Polygon p1 = new Polygon(x, y, 3);
		g.fillPolygon(p1);

		x[0] = 846 + distanceX;
		x[1] = 553 + distanceX;
		x[2] = 553 + distanceX;
		y[0] = 257 + distanceY;
		y[1] = 222 + distanceY;
		y[2] = 257 + distanceY;
		Polygon p2 = new Polygon(x, y, 3);
		g.fillPolygon(p2);
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setAlwaysOnTop(true);
		setFont(new Font("Bookman Old Style", Font.PLAIN, 15));
		setTitle("Infix Calculator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 400);

		contentPane = new JPanel();
		contentPane.setBackground(new Color(64, 149, 195));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel(" Operation");
		lblNewLabel.setBackground(new Color(237, 116, 186));
		lblNewLabel.setOpaque(true);
		lblNewLabel.setForeground(new Color(255, 255, 255));
		lblNewLabel.setFont(new Font("Kristen ITC", Font.PLAIN, 25));
		lblNewLabel.setBounds(34, 11, 143, 43);
		contentPane.add(lblNewLabel);

		lbResult = new JLabel("");
		lbResult.setOpaque(true);
		lbResult.setBackground(new Color(185, 230, 242));
		lbResult.setForeground(new Color(0, 0, 160));
		lbResult.setHorizontalAlignment(SwingConstants.RIGHT);
		lbResult.setFont(new Font("Microsoft JhengHei", Font.BOLD, 45));
		lbResult.setBounds(359, 256, 487, 72);
		contentPane.add(lbResult);

		tfInput = new JTextField();
		tfInput.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 33));
		tfInput.setBounds(10, 53, 864, 72);
		contentPane.add(tfInput);
		tfInput.setColumns(10);

		JButton btnCalculate = new JButton("Calculate");
		btnCalculate.setBackground(new Color(15, 36, 123));
		btnCalculate.setForeground(new Color(206, 244, 255));
		btnCalculate.setFont(new Font("Harlow Solid Italic", Font.PLAIN, 43));
		btnCalculate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!tfInput.getText().isEmpty()) {
					String inputOperation = tfInput.getText();
					lbResult.setText(processOperation(inputOperation));
					if (lbResult.getText().length() >= 25) {
						lbResult.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
					} else if (lbResult.getText().length() >= 17) {
						lbResult.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
					} else {
						lbResult.setFont(new Font("Microsoft JhengHei", Font.BOLD, 45));
					}
				}
			}
		});
		btnCalculate.setBounds(517, 150, 329, 50);
		contentPane.add(btnCalculate);
		JLabel lblAnswer = new JLabel(" Answer");
		lblAnswer.setOpaque(true);
		lblAnswer.setVerticalAlignment(SwingConstants.TOP);
		lblAnswer.setForeground(new Color(206, 244, 255));
		lblAnswer.setBackground(new Color(79, 216, 255));
		lblAnswer.setFont(new Font("Grand Aventure", Font.PLAIN, 31));
		lblAnswer.setBounds(425, 222, 128, 34);
		contentPane.add(lblAnswer);

		textAreaHistory = new JTextArea();
		textAreaHistory.setEnabled(false);
		textAreaHistory.setDisabledTextColor(Color.BLUE);
		textAreaHistory.setFont(new Font("Nirmala UI Semilight", Font.PLAIN, 17));
		textAreaHistory.setBounds(1, 1, 310, 349);
		textAreaHistory.setText("");
		textAreaHistory.setLineWrap(true);
		areaScrollPane = new JScrollPane(textAreaHistory);
		areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		areaScrollPane.setBounds(10, 150, 329, 200);
		contentPane.add(areaScrollPane);

		JLabel lblHisotry = new JLabel(" History");
		lblHisotry.setOpaque(true);
		lblHisotry.setForeground(new Color(255, 255, 128));
		lblHisotry.setFont(new Font("Harlow Solid Italic", Font.PLAIN, 31));
		lblHisotry.setBackground(new Color(182, 24, 24));
		lblHisotry.setBounds(337, 150, 122, 50);
		contentPane.add(lblHisotry);
	}
}
