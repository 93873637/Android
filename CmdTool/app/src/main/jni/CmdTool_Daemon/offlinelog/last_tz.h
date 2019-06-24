#ifndef __LAST_TZ_HEAD__
#define __LAST_TZ_HEAD__

/*
 * Enumeration order for VMID's
 */
enum tzdbg_stats_type {
	TZDBG_BOOT = 0,
	TZDBG_RESET,
	TZDBG_INTERRUPT,
	TZDBG_VMID,
	TZDBG_GENERAL,
	TZDBG_LOG,
	TZDBG_QSEE_LOG,
	TZDBG_HYP_GENERAL,
	TZDBG_HYP_LOG,
	TZDBG_STATS_MAX
};

struct tzdbg_log_pos_t {
	 uint16_t wrap;
	 uint16_t offset;
};

 /*
 * Log ring buffer
 */
struct tzdbg_log_t {
	struct tzdbg_log_pos_t	log_pos;
	/* open ended array to the end of the 4K IMEM buffer */
	uint8_t					log_buf[];
};

struct tzdbg_stat {
	char *name;
	char *data;
};

struct tzdbg {
	struct tzdbg_t *diag_buf;
	struct hypdbg_t *hyp_diag_buf;
	char *disp_buf;
	int debug_tz[TZDBG_STATS_MAX];
	struct tzdbg_stat stat[TZDBG_STATS_MAX];
};

int  last_tz_dbgfs_init(void);
void last_tz_addr_hook(struct tz_dbg_addr_info *p);
#endif
