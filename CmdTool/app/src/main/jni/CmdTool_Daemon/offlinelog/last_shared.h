#ifndef __LAST_SHARED_HEAD__
#define __LAST_SHARED_HEAD__

/*================================================
**  definition for last_debug module
**  The 2 files should be nearly the same:
**  letv_last_debug.h----XBL
**  last_shared.h ---KERNEL
**================================================
*/

/* a 8bit value is devided into 2 parts. and saved in 2 pon register*/
#define PANIC_PON_REG1_OFFSET 0x8E /*reg is QPNP_PON_XVDD_RB_SPARE(x) */
#define PANIC_PON_REG1_MASK (0x7F)
#define PANIC_PON_REG1_SHIFT (0)

#define PANIC_PON_REG2_OFFSET 0x8D /*reg is QPNP_PON_SOFT_RB_SPARE(x) */
#define PANIC_PON_REG2_MASK (0x01)
#define PANIC_PON_REG2_SHIFT (0)

typedef u8 uint8;
typedef u16 uint16;
typedef u32 uint32;
typedef u64 uint64;

#define KERN_MAGIC_FOR_XBL (0x55aaaa55)
#define KERN_VER_LEN (256)
#define CRC16_START_VAL (0x55aa)
#define CRC16_TO_64BIT_ZERO (0x28f)
#define CRC16_TO_32BIT_ZERO (0xc831)
#define CRC16_TO_16BIT_ZERO (0x9fbe)

/* the UFS paritition will be divided into several parts.
* last_kmsg is the first.
*/
#define SEC_GAP (0x1000)
#define LAST_DBG_INFO_OFFSET (0x0)
#define LAST_DBG_INFO_SIZE (0x1000000)

/*last debug offset info in debug parition*/
#define LAST_DBG_MEM_LEN LAST_DBG_INFO_SIZE

#define LAST_DBG_HEAD_MAX_LEN (0x1000)
#define LAST_DBG_HEAD_OFFSET (LAST_DBG_MEM_LEN - LAST_DBG_HEAD_MAX_LEN)

#define LAST_DBG_HEAD_ADDR_MAX_LEN (0x1000)
#define LAST_DBG_HEAD_ADDR_OFFSET \
			(LAST_DBG_HEAD_OFFSET - LAST_DBG_HEAD_ADDR_MAX_LEN)

#define LAST_PANIC_LOG_MAX_LEN (0x2000)
#define LAST_PANIC_LOG_BUF_OFFSET \
			(LAST_DBG_HEAD_ADDR_OFFSET - LAST_PANIC_LOG_MAX_LEN)

#define LAST_TZ_LOG_MAX_LEN (0x2000)
#define LAST_TZ_LOG_BUF_OFFSET \
			(LAST_PANIC_LOG_BUF_OFFSET - LAST_TZ_LOG_MAX_LEN)

#define LAST_RPM_LOG_MAX_LEN (0x2000)
#define LAST_RPM_LOG_BUF_OFFSET \
			(LAST_TZ_LOG_BUF_OFFSET - LAST_RPM_LOG_MAX_LEN)

#define LAST_RPM_CFG_MAX_LEN (0x200)
#define LAST_RPM_CFG_BUF_OFFSET \
			(LAST_RPM_LOG_BUF_OFFSET - LAST_RPM_CFG_MAX_LEN)

#define LAST_INFO_MAX_LEN LAST_RPM_CFG_BUF_OFFSET

#define LAST_TZ_LOG_LEN (0x2000)
#define TZ_LOG_PHY_ADDR (0x06698000)

#define LAST_RPM_LOG_LEN (0x2000)
#define RPM_LOG_PHY_ADDR (0x29FC58)
#define LAST_RPM_CFG_LEN (0x58)
#define RPM_CFG_PHY_ADDR (0x29FC00)

/* data struct definition.
* THEY WERE SUPPOSED TO BE ALLIGNNED BY 64 BIT
*/
struct last_kmsg_info {
	u64 log_first_seq;
	u64 log_next_seq;
	u32 log_first_idx;
	u32 log_next_idx;
	u16 log_first_seq_crc16;
	u16 log_first_idx_crc16;
	u16 log_next_seq_crc16;
	u16 log_next_idx_crc16;
} __packed;

struct last_kmsg_addr_info {
	u64 log_first_seq_addr;
	u64 log_first_seq_crc16_addr;
	u64 log_first_idx_addr;
	u64 log_first_idx_crc16_addr;
	u64 log_next_seq_addr;
	u64 log_next_seq_crc16_addr;
	u64 log_next_idx_addr;
	u64 log_next_idx_crc16_addr;
	u64 log_buf_addr;
	u32 log_buf_len;
	u32 reserved;
} __packed;

struct pon_pm_reason_status {
	u8 pon_reason1;
	u8 pon_reason2;
	u8 warm_reset_reason1;
	u8 warm_reset_reason2;
	u8 poff_reason1;
	u8 poff_reason2;
	u8 soft_reset_reason1;
	u8 soft_reset_reason2;
} __packed;

struct pon_poff_info {
	u64 pm0;
	u64 pm1;
	u32 reset_status_reg;
	u32 reserved0;
} __packed;

struct panic_reason_info {
	u32 panic_reason_crc;
	u32 plog_index_crc;
} __packed;

struct panic_reason_addr_info {
	u64 last_panic_reason_addr;
	u64 last_plog_index_addr;
	u64 panic_log_buf_addr;
	u32 panic_log_buf_len;
	u32 reserved;
} __packed;

struct tz_dbg_addr_info {
	u64 last_tz_addr;
	u32 last_tz_len;
	u32 reserved;
} __packed;

struct last_dbg_info {
	u64 status;
	struct last_kmsg_info last_kmsg;
	struct panic_reason_info panic_info;
	struct pon_poff_info pon_poff;
} __packed;

struct rpm_dbg_addr_info {
	u64 last_rpm_addr;
	u32 last_rpm_len;
	u32 last_rpm_cfg_len;
	u64 last_rpm_cfg_addr;
}__packed;

#define LAST_VERSION_MAGIC (0x12340051)
struct last_dbg_addr_info {
	u32 self_crc;
	u32 reserved;
	u32 last_ver_magic;
	u32 kern_ver_crc;
	u64 kern_ver_addr;
	u64 xbl_copied_flg_addr;
	struct panic_reason_addr_info panic_reason_addr;
	struct last_kmsg_addr_info last_kmsg_addr;
	struct tz_dbg_addr_info tz_dbg_info;
	struct rpm_dbg_addr_info rpm_dbg_info;
} __packed;

#endif
