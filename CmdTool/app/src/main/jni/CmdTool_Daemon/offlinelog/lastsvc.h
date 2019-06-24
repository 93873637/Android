#ifndef __LAST_DEBUG_INFO_H__
#define __LAST_DEBUG_INFO_H__

#define SET_LAST_PANIC_LOG_DATA _IO('P', 1)
#define SET_LAST_PANIC_LOG_INFO _IO('P', 2)
#define GET_PANIC_REASON_ADDR_INFO _IO('P', 3)
#define LAST_PANIC_REASON_INIT _IO('P', 4)
#define GET_PANIC_REASON_STATUS _IO('P', 5)


#define SET_LK_LOG_DATA _IO('L', 1)
#define SET_LK_LOG_INFO _IO('L', 2)
#define SET_LK_PON_POFF_INFO _IO('L', 3)
#define LK_POST_INIT _IO('L', 4)
#define GET_LK_LAST_INFO_ADDR _IO('L', 5)

#define SET_TZ_LOG_DATA _IO('L', 1)
#define SET_RPM_CFG_DATA _IO('L', 1)
#define SET_RPM_LOG_DATA _IO('L', 2)

enum {
	LAST_PANIC_NEW_KERN = 1,
	LAST_PANIC_PMIC_RESET = 2,
};

typedef unsigned char u8;
typedef unsigned short u16;
typedef unsigned int u32;
typedef unsigned long long u64;

#endif
