#ifndef __RAMDUMP_QUALCOMM_HEADER__
#define __RAMDUMP_QUALCOMM_HEADER__

#define SECTION_NAME_LEN 20


typedef unsigned char      uint8;
typedef unsigned short     uint16;
typedef unsigned int       uint32;
typedef unsigned long long uint64;

union type_specific_info
{
	/* Base address of a device memory dump */
	uint64 base_addr;

	/* Not used by SBL dump */
	uint8 cpu_content[6];

	/* Not used by SBL dump */
	uint8 sv_specific[16];
}__packed;


typedef enum
{
	RAW_PARITION_DUMP_RESERVED = 0,

	/* Device memory dump */
	RAW_PARITION_DUMP_DDR_TYPE      = 1,

	/* CPU context, not used */
	RAW_PARITION_DUMP_CPU_CXT_TYPE  = 2,

	/* Silicon Vendor specific data */
	RAW_PARITION_DUMP_SV_TYPE      = 3,

	/* Force it to uint32 size */
	RAW_PARITION_DUMP_MAX      = 0x7FFFFFFF

}boot_raw_partition_dump_section_type;

/**
* This struct represents the header of the whole raw parition ram dump
* size is 56 bytes
*/
struct boot_raw_parition_dump_header
{
/* Signature indicating presence of ram dump */
uint8 signature[8];

/* Version number, should be 0x1000*/
uint32 version;

/* bit 0: dump valid
bit 1: insufficant storage
bit 31:2 reserved, should be 0 */
uint32 validity_flag;

/* Not used by SBL ram dump */
uint64 os_data;

/* Not used by SBL ram dump */
uint8 cpu_context[8];

/* Not used by SBL ram dump */
uint32 reset_trigger;

/* Total size of the actual dump including headers */
uint64 dump_size;

/* Total size required */
uint64 total_dump_size_required;

/* Number of sections included in this dump */
uint32 sections_count;
}__packed;

struct boot_raw_partition_dump_section_header
{
	/* bit 0: dump valid
	bit 1: insufficant storage
	bit 31:2 reserved, should be 0 */
	uint32 validity_flag;

	/*Version number*/
	uint32 section_version;

	/* Type of this section */
	boot_raw_partition_dump_section_type section_type;

	/* Byte offset to the start of this section's data */
	uint64 section_offset;

	/* Total size of the section's data */
	uint64 section_size;

	/* Type specific information,
	we use it to store base address of device memory*/
	union type_specific_info section_info;

	/* Name of this section */
	uint8 section_name[SECTION_NAME_LEN];
}__packed;

#define RAM_DUMP_HEADER_SIGNATURE    {0x52,0x61,0x77,0x5F,0x44,0x6D,0x70,0x21}
#endif
