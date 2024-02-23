#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include "tm4c123gh6pm.h"

#define RST 0x00

void Timer4_init(void);
void Timer5_init(void);
void UART5_init(void);
void PortC_init(void);
void PWM1_init(uint16_t period, uint16_t duty);

/* TIMER4A will periodically be called and stop all actions
	 when there are no actions being sent to the microcontroller.
	 Action depends on COUNT and PRESCLE, occurs every 100ms*/
void TIMER4A_Handler(void);

/* delay Functions Declaration */
void delay_us(int time);
void delay_ms(int time);

/* UART Functions Declaration */
void UART5_Handler(void);
void UART5_Write(unsigned char data);
char UART5_Read(void);
void Write_String(char *str);
void UART5_Newline(void);

void enable_Motors(bool enable);
void update_Motors(uint16_t motor0, uint16_t motor1, uint16_t motor2, uint16_t motor3);
void change_Motor_Duty(bool direction);
void update_Servos(uint16_t servo1, uint16_t servo2, uint16_t servo3);

void move_forward(uint16_t time);
void move_backward(uint16_t time);
void move_left(uint16_t time);
void move_right(uint16_t time);

/* Interrupt Function Declarations */
void DisableInterrupts(void);
void EnableInterrupts(void);
void WaitForInterrupt(void);

uint16_t motor_duty, s0d, s1d, s2d;

int main(void) {
	motor_duty = 1;
	s0d = 2499;
	s1d = 2499;
	s2d = 2499;
	
	Timer4_init();
	Timer5_init();
	UART5_init();
	PortC_init();
	PWM1_init(5000, motor_duty);
	/////////// Enable Global Interrupt Flag ///
	EnableInterrupts(); /* Enable global Interrupt flag (I) */
	
	update_Servos(4999, 4999, 4999);
	
	while(1){
		WaitForInterrupt();
	}
}

void Timer4_init(void) {
	SYSCTL_RCGCTIMER_R |= SYSCTL_RCGCTIMER_R4;	/* enable and provide clock to TIMER4 */
	TIMER4_CTL_R = RST;													/* disable Timer before initialization */
	TIMER4_CFG_R = 0x04;												/* 16-bit option */
	
	TIMER4_TAMR_R = 0x02;												/* periodic mode and down-counter */
	TIMER4_TAILR_R = 50000 - 1;									/* Timer A interval load value register */
	TIMER4_ICR_R = 0x01;												/* clear the TimerA timeout flag*/
	TIMER4_IMR_R |= 0x01;												// Enable TIMER4A Time-Out Interrupt Mask (TATOIM)
	
	TIMER4_CTL_R |= 0x01;												/* enable TimerA after initialization */
	TIMER4_TAPR_R = 100 - 1;
	
	NVIC_PRI17_R = (NVIC_PRI15_R & 0xFF0FFFFF) | 0x00E00000;	// priority 7
	NVIC_EN2_R |= 0x40;																				// enable interrupt
}

void Timer5_init(void) {
	SYSCTL_RCGCTIMER_R |= SYSCTL_RCGCTIMER_R5;	/* enable and provide clock to TIMER5 */
	TIMER5_CTL_R = RST;													/* disable Timer before initialization */
	TIMER5_CFG_R = 0x04;												/* 16-bit option */
	
	TIMER5_TAMR_R = 0x02;												/* periodic mode and down-counter */
	TIMER5_TAILR_R = 50 - 1;										/* Timer A interval load value register */
	TIMER5_TBMR_R = 0x02;												/* periodic mode and down-counter */
	TIMER5_TBILR_R = 50000 - 1;									/* Timer B interval load value register */
	TIMER5_ICR_R = 0x101;												/* clear the TimerA and TimerB timeout flag*/
	
	TIMER5_CTL_R |= 0x101;											/* enable TimerA and TimerB after initialization */
	TIMER5_TAPR_R = 1 - 1;											// Prescalar value.. Can extend the cycle time max 256 times
	TIMER5_TBPR_R = 1 - 1;											// Prescalar value.. Can extend the cycle time max 256 times
}

void UART5_init(void) {
	SYSCTL_RCGC2_R |= SYSCTL_RCGC2_GPIOE;
	delay_us(1);
	
	SYSCTL_RCGCUART_R |= SYSCTL_RCGCUART_R5;
	delay_us(1);
	
	GPIO_PORTE_AMSEL_R = RST;
	GPIO_PORTE_PCTL_R = 0x00110000;
	GPIO_PORTE_DEN_R = 0x30;
	GPIO_PORTE_AFSEL_R = 0x30;
	
	UART5_CTL_R = 0;
	UART5_IBRD_R = 325;
	UART5_FBRD_R = 34;
	UART5_CC_R = 0;
	UART5_LCRH_R = 0x70;
	UART5_IFLS_R &= ~0x11;
	UART5_CTL_R = 0x311;
	
	// Enable interrupt
	UART5_ICR_R |= 0x10;
	
	// Enable UART Interrupt Mask (UARTIM) and set UART Receive Interrupt Mask
	UART5_IM_R |= 0x10;
	
	NVIC_PRI15_R = NVIC_PRI15_R & 0xFFFF0FFF;		// priority 0, need to prioritize when command is received
	NVIC_EN1_R |= 0x20000000;										// enable interrupt
}

void PortC_init(void) {
	SYSCTL_RCGC2_R |= SYSCTL_RCGC2_GPIOC; // 1) F port clock
	delay_us(1);
	
	GPIO_PORTC_LOCK_R = GPIO_LOCK_KEY; // 2) unlock PortC
	GPIO_PORTC_CR_R = 0x0F;
	GPIO_PORTC_AMSEL_R = RST;
	GPIO_PORTC_AFSEL_R = RST;
	GPIO_PORTC_PCTL_R = RST;
	GPIO_PORTC_DIR_R = 0x0F;
	GPIO_PORTC_DEN_R = 0x0F;
	delay_us(10);
}

void PWM1_init(uint16_t period, uint16_t duty) {
	SYSCTL_RCGCPWM_R  |= SYSCTL_RCGCPWM_R1;			// Enable clock on PWM module 1
	SYSCTL_RCGCGPIO_R |= SYSCTL_RCGCGPIO_R0;		// Enable clock on GPIO PORTA
	SYSCTL_RCGCGPIO_R |= SYSCTL_RCGCGPIO_R3;		// Enable clock on GPIO PORTD
	SYSCTL_RCGCGPIO_R |= SYSCTL_RCGCGPIO_R5;		// Enable clock on GPIO PORTF
	delay_us(10);
	
	SYSCTL_RCC_R |= SYSCTL_RCC_USEPWMDIV;				// PWM clock source is PWM clock divider
	SYSCTL_RCC_R |= 0x000E0000;									// divide by 64
	delay_us(10);
	
	GPIO_PORTA_AFSEL_R |= 0xC0;									// PORTA Pins 6-7 AF#5
	GPIO_PORTA_PCTL_R &= 0x00FFFFFF;
	GPIO_PORTA_PCTL_R |= 0x55000000;
	GPIO_PORTA_DEN_R |= 0xC0;										// Digital Enable
	
	GPIO_PORTD_AFSEL_R |= 0x03;									// PORTD Pins 0-1 AF#5
	GPIO_PORTD_PCTL_R &= 0xFFFFFF00;
	GPIO_PORTD_PCTL_R |= 0x00000055;
	GPIO_PORTD_DEN_R |= 0x03;										// Digital Enable
	
	GPIO_PORTF_AFSEL_R |= 0x0E;									// PORTF Pins 1-3 AF#5
	GPIO_PORTF_PCTL_R &= 0xFFFFF000;
	GPIO_PORTF_PCTL_R |= 0x00000555;
	GPIO_PORTF_DEN_R |= 0x0E;										// Digital Enable
	
	PWM1_0_CTL_R &= ~0x03;											// Disable generator and enable down count
	PWM1_1_CTL_R &= ~0x03;
	PWM1_2_CTL_R &= ~0x03;
	PWM1_3_CTL_R &= ~0x03;
	delay_us(1);
	
	PWM1_0_GENA_R = 0x8C;												/* Set PWM output when counter reloaded and clear when matches PWMCMPA */
	PWM1_0_GENB_R = 0x80C;
	PWM1_1_GENA_R = 0x8C;
	PWM1_1_GENB_R = 0x80C;
//	PWM1_2_GENA_R = 0x8C;			//not being used so not setting it
	PWM1_2_GENB_R = 0x80C;
	PWM1_3_GENA_R = 0x8C;
	PWM1_3_GENB_R = 0x80C;
	delay_us(1);
	
	PWM1_0_LOAD_R = period;
	PWM1_1_LOAD_R = period;
	PWM1_2_LOAD_R = period;
	PWM1_3_LOAD_R = period;
	delay_us(1);
	
	PWM1_0_CMPA_R = duty;
	PWM1_0_CMPB_R = duty;
	PWM1_1_CMPA_R = duty;
	PWM1_1_CMPB_R = duty;
//	PWM1_2_CMPA_R = duty;			//not being used so not setting it
	PWM1_2_CMPB_R = duty;
	PWM1_3_CMPA_R = duty;
	PWM1_3_CMPB_R = duty;
	delay_us(1);
	
	PWM1_0_CTL_R = 0x01;												// Enable generators
	PWM1_1_CTL_R = 0x01;
	PWM1_2_CTL_R = 0x01;
	PWM1_3_CTL_R = 0x01;
	delay_us(1);
	
	PWM1_ENABLE_R = 0xEF;												//Enable Interrupts PWM1 Generator 0-3 and 5-7
	delay_us(1);
}

void TIMER4A_Handler(void) {
//	GPIO_PORTF_DATA_R = 0x02;
//	delay_ms(100);
//	GPIO_PORTF_DATA_R = RST;
	
	// Turn off all motors, servos can stay in position
	enable_Motors(false);
	update_Motors(4999, 4999, 4999, 4999);
	
	TIMER4_ICR_R = 0x01; /* clear the TimerA timeout flag*/
}

void delay_us(int time) {
	int i;
	for(i = 0; i < time; i++) {
		TIMER5_ICR_R = 0x01;
		while ((TIMER5_RIS_R & 0x01) == 0);
	}
}

void delay_ms(int time) {
	int i;
	for(i = 0; i < time; i++) {
		TIMER5_ICR_R = 0x100;
		while ((TIMER5_RIS_R & 0x100) == 0);
	}
}

void UART5_Handler(void){
	char c = UART5_Read();
	delay_ms(200);
	
	if(c == 'W'){
		Write_String("forward");
		move_forward(1000);
	} else if(c == 'S') {
		Write_String("backward");
		move_backward(1000);
	} else if(c == 'A') {
		Write_String("?left?");
		move_left(1000);
	} else if(c == 'D') {
		Write_String("?right?");
		move_right(1000);
	} else if(c == 'K'){
		change_Motor_Duty(true);
		Write_String("motor speed decreased");
	} else if(c == 'I'){
		change_Motor_Duty(false);
		Write_String("motor speed increased");
	} else {
		Write_String("try again");
	}
	UART5_Newline();
}

void UART5_Write(unsigned char data) {
	while(UART5_FR_R & UART_FR_BUSY);				/* Wait until send is full */
	UART5_DR_R = data;											/* Send the contents */
}

char UART5_Read(void) {
	while(UART5_FR_R & UART_FR_RXFE);				/* Wait until recieve has data */
	return (unsigned char) UART5_DR_R;			/* Return the contents */
}

void UART5_Newline(void)	{
	Write_String("\r\n");										/* Write carriage return and new line to UART*/
}

void Write_String(char *str) {
	while (*str)														/* Go until the end of the string */
		UART5_Write(*(str++));								/* Write character and increment string pointer */
}

void enable_Motors(bool enable) {
	GPIO_PORTC_DATA_R = enable ? 0x0F : 0x00;
}

void update_Motors(uint16_t motor0, uint16_t motor1, uint16_t motor2, uint16_t motor3) {
	PWM1_0_CMPA_R = motor0;
	PWM1_0_CMPB_R = motor1;
	PWM1_1_CMPA_R = motor2;
	PWM1_1_CMPB_R = motor3;
	
	enable_Motors(true);
	delay_us(1);
}

void change_Motor_Duty(bool direction) {
	update_Motors(4999, 4999, 4999, 4999);
	delay_us(1);
	
	if (direction && motor_duty < 4900){
		motor_duty += 1000;
	} else if (motor_duty > 1000){
		motor_duty -= 1000;
	}
	delay_us(1);
}

void update_Servos(uint16_t servo1, uint16_t servo2, uint16_t servo3) {
	PWM1_2_CMPB_R = servo1;
	PWM1_3_CMPA_R = servo2;
	PWM1_3_CMPB_R = servo3;
	delay_us(1);
}

void move_forward(uint16_t time){
	update_Motors(motor_duty, 4999, motor_duty, 4999);
	delay_ms(time);
}

void move_backward(uint16_t time){
	update_Motors(4999, motor_duty, 4999, motor_duty);
	delay_ms(time);
}

void move_left(uint16_t time){
	update_Motors(motor_duty, 4999, 4999, motor_duty);
	delay_ms(time);
}

void move_right(uint16_t time){
	update_Motors(4999, motor_duty, motor_duty, 4999);
	delay_ms(time);
}

/*********** DisableInterrupts ***************
*
* disable interrupts
*
* inputs: none
* outputs: none
*/
void DisableInterrupts(void) {
	__asm (" CPSID I\n");
}

/*********** EnableInterrupts ***************
*
* emable interrupts
*
* inputs: none
* outputs: none
*/
void EnableInterrupts(void) {
	__asm (" CPSIE I\n");
}

/*********** WaitForInterrupt ************************
*
* go to low power mode while waiting for the next interrupt
*
* inputs: none
* outputs: none
*/
void WaitForInterrupt(void) {
	__asm (" WFI\n");
}
