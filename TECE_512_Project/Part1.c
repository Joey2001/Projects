#include "tm4c123gh6pm.h"  
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

/* ********** Pins for UART and Ultrasonic Sensor ********* */
#define PA4		0x10		/*            PORTA PIN_4             */
#define PE45	0x30		/*            PORTE PINS_4&5          */
#define PF0		0x01		/*            PORTF PIN_0             */
#define ALL		0xFF		/*           PORTB PINS_0-7           */
#define RST		0x00		/*               RESET                */
#define IBD		325			/*  (50 MHz)/(16 Clkdiv * 9600 baud)  */
#define FBD		33			/*      (.52083 * 64 + .5) = 33.8     */
/* ********** Pins for UART and Ultrasonic Sensor ********* */

/* ******************** LCD Definitions ******************* */
#define RS		0x01		/*          RS -> PB0 (0x01)          */
#define RW		0x02    /*          RW -> PB1 (0x02)          */
#define EN		0x04  	/*          EN -> PB2 (0x04)          */
#define CLR		0x01		/*           Clear display            */
#define _2L4B	0x28		/* 2 lines, 5x7 character, 4-bit data */
#define AIC		0x06		/*     Automatic Increment cursor     */
#define DOCB	0x0F		/*     Display on, cursor blinking    */
#define CRST	0x80		/*            Cursor RESET            */
/* ******************** LCD Definitions ******************* */

/* Initialization for UART, LCD, and ultrasonic sensor */
void ultrasonic_init(void);
void UART5_init(void);
void LCD_Init(void);

/* Ultrasonic Sensor Function Declaration */
uint32_t Measure_distance(void);

/* UART Functions Declaration */
void UART_Write(unsigned char data);
char UART_Read(void);
void write_string(char *str);
void UART_newline(void);

/* Delay Functions Declaration */
void delay_us(int time);
void delay_ms(int time);

/* LCD Functions Declaration */
void LCD_WriteNibble(unsigned char data, unsigned char control);
void LCD_WriteString(char* str);
void LCD_Cmd(unsigned char command);
void LCD_Data(unsigned char data);

static uint32_t distance;																							/* Measured distance value in cm */
static char mesg[20];																									/* Message string for UART and LCD */

int main(void) {
	ultrasonic_init();
	UART5_init();
	LCD_Init();
	
	delay_ms(500);
	
	while(1) {
		distance					 = Measure_distance();													/* Get the distance */
		
		if(distance >> 20)
			sprintf(mesg, "OUT OF RANGE");																	/* Prints when distance is 2^20 */
		else
			sprintf(mesg, "Distance: %d cm", distance);											/* Prints distance when in range */
	
		LCD_Cmd(CLR);																											/* Clear LCD */
		LCD_Cmd(CRST);               																			/* Force cursor to beginning of 1st line */
		LCD_WriteString(mesg);																						/* Write the string on LCD */
		
		UART_newline();																										/* Creates a new line for UART */
		write_string(mesg);																								/* Writes string over UART */
		
		delay_ms(1500);																										/* Delay 1.5 s to let the LCD diplays the data */
		
		if (UART5_FR_R & UART_FR_RXFF) {																	/* implement features later (conditional works) */
			/* ************************ TODO ************************ */
			/* ************************ TODO ************************ */
			/* ************************ TODO ************************ */
			/* ************************ TODO ************************ */
		}
	}

}
void ultrasonic_init(void) {
	SYSCTL_RCGC2_R 			|= SYSCTL_RCGC2_GPIOF;													/* Enable clock gating to PORTF */
	delay_us(0);																												/* Need time to set clock gating */
	SYSCTL_RCGCTIMER_R	|= SYSCTL_RCGCTIMER_R0;													/* Enable clock to Timer Block 0 */
	SYSCTL_RCGCGPIO_R		|= SYSCTL_RCGCGPIO_R5;													/* Enable clock to PORTF */
	
	GPIO_PORTF_LOCK_R		 = GPIO_LOCK_KEY;																/* UNLOCK PORTF */
	GPIO_PORTF_CR_R			 = PF0;																					/* Allow changes to PF0 */
	GPIO_PORTF_AMSEL_R	 = RST;																					/* Disable analog function */
	GPIO_PORTF_DIR_R		 = (unsigned long) ~PF0;												/* Set PF0 as a digial input pin */
	GPIO_PORTF_DEN_R		 = PF0;																					/* Enable digital pin PF0 */
	GPIO_PORTF_AFSEL_R	|= PF0;																					/* Use PF0 alternate function */
	GPIO_PORTF_PCTL_R		&= (unsigned long) ~PF0;												/* GPIO clear bit PCTL for PF0 */
	GPIO_PORTF_PCTL_R		|= GPIO_PCTL_PF0_T0CCP0;												/* Alternate function 7 for PF0 */
	GPIO_PORTF_PUR_R		 = RST;																					/* Disable pullup resistors */
	GPIO_PORTF_PDR_R		 = PF0;																					/* Enable pulldown resistor PF0 */
	
	SYSCTL_RCGCGPIO_R		|= SYSCTL_RCGCGPIO_R0;     											/* Enable clock to PORTA */
	
	GPIO_PORTA_DIR_R		|= PA4;         																/* Set PA4 as a digial output pin */
	GPIO_PORTA_DEN_R		|= PA4;         																/* Make PA4 as digital pin */
	
	TIMER0_CTL_R				&= (unsigned long) ~TIMER_CTL_TAEN;							/* Disable timer0A during setup */
	TIMER0_CFG_R				 = TIMER_CFG_16_BIT;				       						 	/* 16-bit option */ 
	TIMER0_TAMR_R				 = 0x17;       																	/* Up-count, edge-time, capture mode */
	TIMER0_CTL_R				|= TIMER_CTL_TAEVENT_BOTH;											/* Capture the both edges */
	TIMER0_CTL_R				|= TIMER_CTL_TAEN;															/* Enable Timer A after initialization */
}

void UART5_init(void) {
	SYSCTL_RCGCUART_R 	|= SYSCTL_RCGCUART_R5;													/* Enable clock to UART5 */
	SYSCTL_RCGCGPIO_R		|= SYSCTL_RCGCGPIO_R4;													/* Enable clock to PORTE */
	
	UART5_CTL_R					 = RST;																					/* UART5 module disbable */
	UART5_IBRD_R  			 = IBD;																					/* For 9600 baud rate */
	UART5_FBRD_R				 = FBD;																					/* For 9600 baud rate */
	UART5_CC_R					 = UART_CC_CS_SYSCLK;														/* Select system clock */
	UART5_LCRH_R				 = UART_LCRH_WLEN_8;														/* Data lenght 8-bit, not parity bit, no FIFO */
	UART5_CTL_R					|= (UART_CTL_RXE | UART_CTL_TXE);								/* Enable UART5 Rx and Tx */
	UART5_CTL_R					|= UART_CTL_UARTEN;															/* Enable UART5 module */
	
	GPIO_PORTE_DEN_R		 = PE45;																				/* Enable digital pins PE4 and PE5 */
	GPIO_PORTE_AFSEL_R	 = PE45;																				/* Use PE4 and PE5 alternate function */
	GPIO_PORTE_AMSEL_R	 = RST;																					/* Disable analog function */
	GPIO_PORTE_PCTL_R		&= (unsigned long) ~0x00FF0000;									/* GPIO clear bit PCTL for PE4 and PE5 */
	GPIO_PORTE_PCTL_R		|= GPIO_PCTL_PE4_U5RX;													/* Alternate function for PE4 */
	GPIO_PORTE_PCTL_R		|= GPIO_PCTL_PE5_U5TX;													/* Alternate function for PE5 */
}

void LCD_Init(void) {
	SYSCTL_RCGCGPIO_R		|= SYSCTL_RCGCGPIO_R1;   												/* Enable clock for PORTB */
	
	delay_ms(10);
	
  GPIO_PORTB_DIR_R		 = ALL;             														/* Set all PORTB pins as output */
	GPIO_PORTB_DEN_R		 = ALL;             														/* Enable all PORTB pins digital IO */
	
	LCD_Cmd(_2L4B);																											/* 2 lines, 5x7 character, 4-bit data */
	LCD_Cmd(AIC);																												/* Automatic Increment cursor */
	LCD_Cmd(CLR);																												/* Clear display */
	LCD_Cmd(DOCB);																											/* Display on, cursor blinking */
}

uint32_t Measure_distance(void) {
	uint32_t delta_T, fallEdge = 0, riseEdge = 0;
	
	/* *********************************** 10us trigger PULSE *********************************** */
	GPIO_PORTA_DATA_R		&= (unsigned long) ~PA4;												/*  Make trigger pin low  */
	delay_us(10);																												/*  10 microsecond delay  */
	GPIO_PORTA_DATA_R		|= PA4;																					/*  Make trigger pin high */
	delay_us(10);																												/*  10 microsecond delay  */
	GPIO_PORTA_DATA_R		&= (unsigned long) ~PA4;												/*  Make trigger  pin low */
	/* *********************************** 10us trigger PULSE *********************************** */
	
	TIMER0_ICR_R				 = TIMER_ICR_CAECINT;														/* Clear TIMER0A capture flag */
	while((TIMER0_RIS_R & TIMER_RIS_CAERIS) == 0);											/* Wait until captured */
	riseEdge						 = (uint32_t) TIMER0_TAR_R;											/* Save the rising edge of the pulse */
		
	TIMER0_ICR_R				 = TIMER_ICR_CAECINT;														/* Clear TIMER0A capture flag */
	while((TIMER0_RIS_R & TIMER_RIS_CAERIS) == 0);											/* Wait until captured */
	fallEdge					 	 = (uint32_t) TIMER0_TAR_R;											/* Save the falling edge of the pulse */
	
	delta_T							 = fallEdge - riseEdge;													/* Time of the pulse */
	fallEdge						 = (delta_T >> 17) > 3;													/* Reusing variable to detect range */
	delta_T							 = (uint32_t) ((((delta_T) >> 3) / 375) + .5);	/* Convert time to distance */
	
	return fallEdge ? (1 << 20) : delta_T;															/* Returns distance unless too large */
}

void UART_Write(unsigned char data) {
	while(UART5_FR_R & UART_FR_TXFF);																		/* Wait until send is full */
	UART5_DR_R					 = data;																				/* Send the contents */
}

char UART_Read(void) {
	while(UART5_FR_R & UART_FR_RXFE);																		/* Wait until recieve has data */
	return (unsigned char) UART5_DR_R;																	/* Return the contents */
}

void UART_newline(void)	{
	write_string("\r\n");																								/* Write carriage return and new line to UART*/
}

void write_string(char *str) {
	while (*str)																												/* Go until the end of the string */
		UART_Write(*(str++));																							/* Write character and increment string pointer */
}

void delay_us(int time){
	int i;
	SYSCTL_RCGCTIMER_R	|= SYSCTL_RCGCTIMER_R1;													/* Enable clock to Timer Block 1 */
	
	TIMER1_CTL_R				 = RST;																					/* Disable timer1A during setup */
	TIMER1_CFG_R				 = TIMER_CFG_16_BIT;				       							/* 16-bit option */ 
	TIMER1_TAMR_R				 = TIMER_TAMR_TAMR_PERIOD;       								/* Periodic mode and down-counter */
	TIMER1_TAILR_R			 = 50;																					/* Timer1A interval load value reg */
	TIMER1_ICR_R				 = TIMER_ICR_TATOCINT;													/* Clear the Timer1A timeout flag */
	TIMER1_CTL_R				|= TIMER_CTL_TAEN;															/* Enable Timer1A after initialization */
	
	for(i = 0; i < time; i++) {
		while(TIMER1_RIS_R & (unsigned long) ~TIMER_RIS_TATORIS);					/* Wait until timeout */
		TIMER1_ICR_R			 = TIMER_ICR_TATOCINT;													/* Reset timer */
	}
}

void delay_ms(int time){
	int i;
	for(i = 0; i < time; i++)
		delay_us(1000);
}

void LCD_WriteNibble(unsigned char data, unsigned char control) {
	data								&= 0xF0;																				/* Clear lower nibble for control */
	control							&= 0x0F;																				/* Clear upper nibble for control */
	
	GPIO_PORTB_DATA_R		 = data | control;															/* Include RS value (command or data ) with data  */
	GPIO_PORTB_DATA_R		 = data | control | EN;													/* Pulse EN */
	
	delay_us(5);																												/* Delay to pulse EN */
	
	GPIO_PORTB_DATA_R		 = data | control;															/* End pulse */
	GPIO_PORTB_DATA_R		 = RST;                      										/* Clear the Data */
}

void LCD_WriteString(char * str) {  
	while(*str)
		LCD_Data(*(str++));
}

void LCD_Cmd(unsigned char command) {
	LCD_WriteNibble(command & 0xF0 , 0);    														/* Upper nibble first */
	LCD_WriteNibble((unsigned char) (command << 4) , 0);			 					/* Lower nibble last */
	
	command < 4 ? delay_ms(2) : delay_us(40);
}

void LCD_Data(unsigned char data) {
	LCD_WriteNibble(data & 0xF0 , RS);   																/* Upper nibble first */
	LCD_WriteNibble((unsigned char) (data << 4) , RS);     							/* Lower nibble last */
	
	delay_us(40);																												/* Delay for LCD (MCU is faster than LCD) */
}
